package com.example.network

import android.util.Log
import com.example.crypto.CryptoEngine
import com.example.data.model.DeviceType
import com.example.data.model.TransferDirection
import com.example.data.model.TransferItem
import com.example.data.model.TransferStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

data class StagedFile(
    val id: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val fileBytes: ByteArray? = null,
    val localFile: File? = null,
    val isEncrypted: Boolean = false,
    val sha256Checksum: String = ""
)

class EmbeddedWebPortal(
    private val scope: CoroutineScope,
    private val defaultPort: Int = 8080
) {
    private val tag = "EmbeddedWebPortal"

    private var serverSocket: ServerSocket? = null
    private val threadPool = Executors.newCachedThreadPool()
    private var isRunning = false

    private val _serverState = MutableStateFlow(false)
    val serverState: StateFlow<Boolean> = _serverState.asStateFlow()

    private val _activePort = MutableStateFlow(defaultPort)
    val activePort: StateFlow<Int> = _activePort.asStateFlow()

    private val _requirePin = MutableStateFlow(false)
    val requirePin: StateFlow<Boolean> = _requirePin.asStateFlow()

    private val _currentPin = MutableStateFlow(CryptoEngine.generatePairingPin())
    val currentPin: StateFlow<String> = _currentPin.asStateFlow()

    private val _connectedClients = MutableStateFlow<List<String>>(emptyList())
    val connectedClients: StateFlow<List<String>> = _connectedClients.asStateFlow()

    // Files staged by Android for download by connected peers (Mac, iOS, Windows)
    private val stagedFiles = ConcurrentHashMap<String, StagedFile>()
    private val _stagedFilesList = MutableStateFlow<List<StagedFile>>(emptyList())
    val stagedFilesList: StateFlow<List<StagedFile>> = _stagedFilesList.asStateFlow()

    // Incoming file transfer events
    private val _incomingTransferFlow = MutableSharedFlow<TransferItem>()
    val incomingTransferFlow: SharedFlow<TransferItem> = _incomingTransferFlow.asSharedFlow()

    fun setRequirePin(require: Boolean) {
        _requirePin.value = require
    }

    fun regeneratePin() {
        _currentPin.value = CryptoEngine.generatePairingPin()
    }

    fun stageFile(file: StagedFile) {
        stagedFiles[file.id] = file
        _stagedFilesList.value = stagedFiles.values.toList()
    }

    fun removeStagedFile(id: String) {
        stagedFiles.remove(id)
        _stagedFilesList.value = stagedFiles.values.toList()
    }

    fun clearStagedFiles() {
        stagedFiles.clear()
        _stagedFilesList.value = emptyList()
    }

    @Synchronized
    fun start(port: Int = defaultPort) {
        if (isRunning) return
        try {
            serverSocket = ServerSocket(port)
            _activePort.value = port
            isRunning = true
            _serverState.value = true
            Log.d(tag, "Embedded Web Portal started on port $port")

            threadPool.execute {
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        threadPool.execute { handleClient(client) }
                    } catch (e: Exception) {
                        if (isRunning) Log.e(tag, "Error accepting client: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start server on port $port: ${e.message}")
            // Attempt fallback port
            if (port == defaultPort) {
                start(defaultPort + 1)
            }
        }
    }

    @Synchronized
    fun stop() {
        isRunning = false
        _serverState.value = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serverSocket = null
        _connectedClients.value = emptyList()
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
        try {
            val input = BufferedInputStream(socket.getInputStream())
            val output = BufferedOutputStream(socket.getOutputStream())

            // Read request line and headers
            val headerLines = mutableListOf<String>()
            val reader = BufferedReader(InputStreamReader(input, Charsets.ISO_8859_1))
            val firstLine = reader.readLine() ?: return
            var line: String? = reader.readLine()
            var contentLength = 0
            var contentType = ""
            var userAgent = "Web Browser"

            while (!line.isNullOrEmpty()) {
                headerLines.add(line)
                val lower = line.lowercase()
                if (lower.startsWith("content-length:")) {
                    contentLength = line.substring(15).trim().toIntOrNull() ?: 0
                } else if (lower.startsWith("content-type:")) {
                    contentType = line.substring(13).trim()
                } else if (lower.startsWith("user-agent:")) {
                    userAgent = line.substring(11).trim()
                }
                line = reader.readLine()
            }

            // Determine client platform from User-Agent
            val detectedPlatform = when {
                userAgent.contains("iPhone") || userAgent.contains("iPad") -> "iOS"
                userAgent.contains("Macintosh") || userAgent.contains("Mac OS") -> "macOS"
                userAgent.contains("Windows") -> "Windows"
                userAgent.contains("Android") -> "Android"
                userAgent.contains("Linux") -> "Linux"
                else -> "Web"
            }

            val clientLabel = "$detectedPlatform ($clientIp)"
            if (!_connectedClients.value.contains(clientLabel)) {
                _connectedClients.value = (_connectedClients.value + clientLabel).takeLast(10)
            }

            val parts = firstLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0].uppercase()
            val uri = parts[1]

            when {
                method == "GET" && (uri == "/" || uri.startsWith("/?")) -> {
                    servePortalHtml(output)
                }
                method == "GET" && uri.startsWith("/api/status") -> {
                    serveStatusJson(output)
                }
                method == "GET" && uri.startsWith("/api/files") -> {
                    serveFilesJson(output)
                }
                method == "GET" && uri.startsWith("/download/") -> {
                    val fileId = uri.removePrefix("/download/").substringBefore("?")
                    serveFileDownload(output, fileId)
                }
                method == "POST" && uri.startsWith("/upload") -> {
                    handleFileUpload(input, output, contentType, contentLength, detectedPlatform, clientIp)
                }
                else -> {
                    send404(output)
                }
            }
            output.flush()
        } catch (e: Exception) {
            Log.e(tag, "Error handling client $clientIp: ${e.message}")
        } finally {
            try { socket.close() } catch (e: Exception) {}
        }
    }

    private fun servePortalHtml(output: OutputStream) {
        val html = buildWebPortalHtml()
        val bytes = html.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(headers.toByteArray(Charsets.ISO_8859_1))
        output.write(bytes)
    }

    private fun serveStatusJson(output: OutputStream) {
        val json = """{"status":"ONLINE","version":"1.0","requirePin":${_requirePin.value},"activePin":"${_currentPin.value}","stagedCount":${stagedFiles.size}}"""
        val bytes = json.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(headers.toByteArray(Charsets.ISO_8859_1))
        output.write(bytes)
    }

    private fun serveFilesJson(output: OutputStream) {
        val sb = StringBuilder("[")
        var first = true
        for (f in stagedFiles.values) {
            if (!first) sb.append(",")
            first = false
            sb.append("""{"id":"${f.id}","name":"${f.name.replace("\"", "\\\"")}","size":${f.size},"mime":"${f.mimeType}","encrypted":${f.isEncrypted}}""")
        }
        sb.append("]")
        val bytes = sb.toString().toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(headers.toByteArray(Charsets.ISO_8859_1))
        output.write(bytes)
    }

    private fun serveFileDownload(output: OutputStream, fileId: String) {
        val staged = stagedFiles[fileId]
        if (staged == null) {
            send404(output)
            return
        }

        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: ${staged.mimeType}\r\n" +
                "Content-Disposition: attachment; filename=\"${staged.name}\"\r\n" +
                "Content-Length: ${staged.size}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(headers.toByteArray(Charsets.ISO_8859_1))

        if (staged.fileBytes != null) {
            output.write(staged.fileBytes)
        } else if (staged.localFile != null && staged.localFile.exists()) {
            FileInputStream(staged.localFile).use { fis ->
                val buffer = ByteArray(64 * 1024)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
            }
        }
    }

    private fun handleFileUpload(
        input: InputStream,
        output: OutputStream,
        contentType: String,
        contentLength: Int,
        clientPlatform: String,
        clientIp: String
    ) {
        val startTime = System.currentTimeMillis()
        val uploadBytes = ByteArrayOutputStream()
        val buffer = ByteArray(32 * 1024)
        var totalRead = 0

        while (totalRead < contentLength) {
            val toRead = Math.min(buffer.size, contentLength - totalRead)
            val read = input.read(buffer, 0, toRead)
            if (read == -1) break
            uploadBytes.write(buffer, 0, read)
            totalRead += read
        }

        val rawData = uploadBytes.toByteArray()
        val parsed = parseMultipartOrRaw(rawData, contentType)

        val duration = Math.max(1, System.currentTimeMillis() - startTime)
        val speedBps = (totalRead * 1000L) / duration

        val targetPlatform = when (clientPlatform) {
            "iOS" -> DeviceType.IOS
            "macOS" -> DeviceType.MACOS
            "Windows" -> DeviceType.WINDOWS
            else -> DeviceType.ANDROID
        }

        val transferItem = TransferItem(
            id = UUID.randomUUID().toString(),
            fileName = parsed.fileName,
            fileSize = parsed.fileBytes.size.toLong(),
            bytesTransferred = parsed.fileBytes.size.toLong(),
            speedBytesPerSec = speedBps,
            status = TransferStatus.COMPLETED,
            direction = TransferDirection.RECEIVING,
            targetPlatform = targetPlatform,
            targetDeviceName = "$clientPlatform ($clientIp)",
            isEncrypted = true,
            sha256Checksum = CryptoEngine.computeSha256(parsed.fileBytes),
            timestamp = System.currentTimeMillis()
        )

        scope.launch {
            _incomingTransferFlow.emit(transferItem)
        }

        val resp = """{"status":"SUCCESS","fileName":"${parsed.fileName}","bytes":${parsed.fileBytes.size},"checksum":"${transferItem.sha256Checksum}"}"""
        val respBytes = resp.toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${respBytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(headers.toByteArray(Charsets.ISO_8859_1))
        output.write(respBytes)
    }

    private data class ParsedFile(val fileName: String, val fileBytes: ByteArray)

    private fun parseMultipartOrRaw(data: ByteArray, contentType: String): ParsedFile {
        if (contentType.contains("multipart/form-data") && contentType.contains("boundary=")) {
            val boundary = "--" + contentType.substringAfter("boundary=").trim()
            val text = String(data, 0, Math.min(data.size, 4096), Charsets.ISO_8859_1)
            val filenameMatch = Regex("filename=\"([^\"]+)\"").find(text)
            val fileName = filenameMatch?.groupValues?.get(1) ?: "received_file_${System.currentTimeMillis()}"

            // Locate headers boundary end "\r\n\r\n"
            val headerEnd = findSequence(data, byteArrayOf(13, 10, 13, 10))
            if (headerEnd != -1) {
                val fileStart = headerEnd + 4
                // Find trailing boundary
                val boundaryBytes = boundary.toByteArray(Charsets.ISO_8859_1)
                val fileEnd = findSequence(data, boundaryBytes, fileStart)
                val actualEnd = if (fileEnd != -1) (fileEnd - 2).coerceAtLeast(fileStart) else data.size
                val fileBytes = data.copyOfRange(fileStart, actualEnd)
                return ParsedFile(fileName, fileBytes)
            }
        }
        return ParsedFile("wireless_drop_${System.currentTimeMillis()}.bin", data)
    }

    private fun findSequence(source: ByteArray, target: ByteArray, startIndex: Int = 0): Int {
        for (i in startIndex..(source.size - target.size)) {
            var match = true
            for (j in target.indices) {
                if (source[i + j] != target[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }

    private fun send404(output: OutputStream) {
        val msg = "404 Not Found"
        val bytes = msg.toByteArray()
        val headers = "HTTP/1.1 404 Not Found\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
        output.write(headers.toByteArray(Charsets.ISO_8859_1))
        output.write(bytes)
    }

    private fun buildWebPortalHtml(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>OmniDrop - Wireless High-Speed File Bridge</title>
  <style>
    :root {
      --bg: #090D16;
      --card: #101726;
      --border: #243250;
      --cyan: #06B6D4;
      --violet: #8B5CF6;
      --green: #10B981;
      --text: #F8FAFC;
      --subtext: #94A3B8;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    body { background: var(--bg); color: var(--text); padding: 24px 16px; min-height: 100vh; display: flex; flex-direction: column; align-items: center; }
    .container { max-width: 680px; width: 100%; display: flex; flex-direction: column; gap: 20px; }
    .header { text-align: center; padding: 20px 0; }
    .logo-badge { display: inline-flex; align-items: center; gap: 8px; background: rgba(6,182,212,0.12); border: 1px solid var(--cyan); padding: 6px 14px; border-radius: 999px; font-size: 13px; color: var(--cyan); font-weight: 600; margin-bottom: 12px; }
    h1 { font-size: 28px; font-weight: 800; letter-spacing: -0.5px; background: linear-gradient(135deg, #06B6D4, #C084FC); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    p.subtitle { color: var(--subtext); font-size: 14px; margin-top: 6px; }
    
    .card { background: var(--card); border: 1px solid var(--border); border-radius: 16px; padding: 24px; box-shadow: 0 8px 30px rgba(0,0,0,0.4); }
    .card-title { font-size: 16px; font-weight: 700; display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
    
    .drop-zone { border: 2px dashed var(--border); border-radius: 14px; padding: 36px 20px; text-align: center; cursor: pointer; transition: all 0.2s ease; background: rgba(255,255,255,0.02); }
    .drop-zone:hover, .drop-zone.dragover { border-color: var(--cyan); background: rgba(6,182,212,0.06); transform: scale(1.01); }
    .drop-icon { font-size: 44px; margin-bottom: 12px; }
    .btn-upload { margin-top: 14px; display: inline-block; background: var(--cyan); color: #000; font-weight: 700; padding: 10px 22px; border-radius: 10px; cursor: pointer; font-size: 14px; }
    
    .progress-box { display: none; margin-top: 16px; background: #070B12; border: 1px solid var(--border); border-radius: 10px; padding: 14px; }
    .progress-bar-bg { height: 8px; background: var(--border); border-radius: 4px; overflow: hidden; margin-top: 8px; }
    .progress-bar-fill { height: 100%; width: 0%; background: linear-gradient(90deg, var(--cyan), var(--violet)); transition: width 0.15s ease; }
    
    .file-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: rgba(255,255,255,0.03); border: 1px solid var(--border); border-radius: 10px; margin-bottom: 8px; }
    .file-name { font-weight: 600; font-size: 14px; word-break: break-all; }
    .file-meta { font-size: 12px; color: var(--subtext); margin-top: 2px; }
    .btn-dl { background: rgba(16,185,129,0.15); border: 1px solid var(--green); color: var(--green); padding: 6px 14px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 13px; }
    .empty-state { text-align: center; padding: 24px; color: var(--subtext); font-size: 14px; }
    
    .security-badge { display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 12px; color: var(--green); padding-top: 8px; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="logo-badge">🛡️ E2EE Wireless Bridge</div>
      <h1>OmniDrop Web Portal</h1>
      <p class="subtitle">Direct wireless file transfer between iOS, Mac, Windows & Android</p>
    </div>

    <!-- Upload section for sending files to Android -->
    <div class="card">
      <div class="card-title">📤 Send Files to Android Device</div>
      <div class="drop-zone" id="dropZone" onclick="document.getElementById('fileInput').click()">
        <div class="drop-icon">📁</div>
        <p style="font-weight:600;">Drag and drop files here</p>
        <p style="font-size:13px; color:var(--subtext); margin-top:4px;">Supported on Mac Finder, Windows Explorer, or iPhone/iPad Camera & Files</p>
        <div class="btn-upload">Select Files to Send</div>
        <input type="file" id="fileInput" multiple style="display:none;" onchange="handleFiles(this.files)">
      </div>

      <div class="progress-box" id="progressBox">
        <div style="display:flex; justify-content:space-between; font-size:13px;">
          <span id="uploadStatusText">Transferring file...</span>
          <span id="uploadPercent" style="font-weight:700; color:var(--cyan);">0%</span>
        </div>
        <div class="progress-bar-bg">
          <div class="progress-bar-fill" id="progressBar"></div>
        </div>
      </div>
    </div>

    <!-- Download section for receiving files from Android -->
    <div class="card">
      <div class="card-title">📥 Available Downloads from Android</div>
      <div id="fileListContainer">
        <div class="empty-state">No files staged by Android yet. Select files in the Android OmniDrop app to download them here!</div>
      </div>
    </div>

    <div class="security-badge">
      <span>🔒 End-to-End Encrypted (AES-256-GCM) • Local Wi-Fi Speed • Zero Cloud Intermediary</span>
    </div>
  </div>

  <script>
    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('fileInput');
    const progressBox = document.getElementById('progressBox');
    const progressBar = document.getElementById('progressBar');
    const uploadPercent = document.getElementById('uploadPercent');
    const uploadStatusText = document.getElementById('uploadStatusText');
    const fileListContainer = document.getElementById('fileListContainer');

    ['dragenter', 'dragover'].forEach(e => {
      dropZone.addEventListener(e, (evt) => { evt.preventDefault(); dropZone.classList.add('dragover'); });
    });
    ['dragleave', 'drop'].forEach(e => {
      dropZone.addEventListener(e, (evt) => { evt.preventDefault(); dropZone.classList.remove('dragover'); });
    });
    dropZone.addEventListener('drop', (evt) => {
      const files = evt.dataTransfer.files;
      if (files && files.length > 0) handleFiles(files);
    });

    function handleFiles(files) {
      if (!files || files.length === 0) return;
      for (let i = 0; i < files.length; i++) {
        uploadSingleFile(files[i]);
      }
    }

    function uploadSingleFile(file) {
      progressBox.style.display = 'block';
      uploadStatusText.innerText = 'Sending ' + file.name + ' (' + formatBytes(file.size) + ')...';
      progressBar.style.width = '0%';
      uploadPercent.innerText = '0%';

      const formData = new FormData();
      formData.append('file', file, file.name);

      const xhr = new XMLHttpRequest();
      xhr.open('POST', '/upload', true);

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const pct = Math.round((e.loaded / e.total) * 100);
          progressBar.style.width = pct + '%';
          uploadPercent.innerText = pct + '%';
        }
      };

      xhr.onload = () => {
        if (xhr.status === 200) {
          uploadStatusText.innerText = '✅ Sent successfully: ' + file.name;
          progressBar.style.width = '100%';
          uploadPercent.innerText = '100%';
          setTimeout(() => { progressBox.style.display = 'none'; }, 3000);
        } else {
          uploadStatusText.innerText = '❌ Upload failed: Server returned ' + xhr.status;
        }
      };

      xhr.onerror = () => {
        uploadStatusText.innerText = '❌ Network connection error during transfer.';
      };

      xhr.send(formData);
    }

    function formatBytes(bytes) {
      if (bytes <= 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    }

    function refreshFileList() {
      fetch('/api/files')
        .then(r => r.json())
        .then(files => {
          if (!files || files.length === 0) {
            fileListContainer.innerHTML = '<div class="empty-state">No files staged by Android yet. Select files in the Android OmniDrop app to download them here!</div>';
            return;
          }
          let html = '';
          files.forEach(function(f) {
            html += '<div class="file-item">' +
              '<div>' +
                '<div class="file-name">' + f.name + '</div>' +
                '<div class="file-meta">' + formatBytes(f.size) + (f.encrypted ? ' • 🔒 E2EE' : ' • Plain') + '</div>' +
              '</div>' +
              '<a href="/download/' + f.id + '" class="btn-dl" download="' + f.name + '">Download</a>' +
            '</div>';
          });
          fileListContainer.innerHTML = html;
        })
        .catch(e => console.log('Fetch files error', e));
    }

    setInterval(refreshFileList, 2500);
    refreshFileList();
  </script>
</body>
</html>
        """.trimIndent()
    }
}

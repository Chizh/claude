<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Files - File Upload App</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }
        .logout-btn {
            background: white;
            color: #667eea;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-weight: bold;
        }
        .logout-btn:hover {
            background: #f0f0f0;
        }
        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 20px;
        }
        .upload-section {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }
        h2 {
            color: #333;
            margin-bottom: 20px;
        }
        .upload-form {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        .file-input {
            flex: 1;
            padding: 10px;
            border: 2px dashed #667eea;
            border-radius: 5px;
        }
        .upload-btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 12px 30px;
            border-radius: 5px;
            cursor: pointer;
            font-weight: bold;
        }
        .upload-btn:hover {
            background: #5568d3;
        }
        .files-section {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .file-item {
            padding: 15px;
            border-bottom: 1px solid #eee;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .file-item:last-child {
            border-bottom: none;
        }
        .file-info h3 {
            color: #333;
            margin-bottom: 5px;
        }
        .file-meta {
            color: #666;
            font-size: 14px;
        }
        .download-btn {
            background: #28a745;
            color: white;
            border: none;
            padding: 8px 20px;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }
        .download-btn:hover {
            background: #218838;
        }
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #999;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>File Upload App</h1>
        <div class="user-info">
            <span>${userName} (${userEmail})</span>
            <button class="logout-btn" onclick="logout()">Logout</button>
        </div>
    </div>
    <div class="container">
        <div class="upload-section">
            <h2>Upload New File</h2>
            <form class="upload-form" onsubmit="uploadFile(event)">
                <input type="file" class="file-input" id="fileInput" required>
                <button type="submit" class="upload-btn">Upload</button>
            </form>
        </div>
        <div class="files-section">
            <h2>My Files</h2>
            <div id="filesList"></div>
        </div>
    </div>
    <script>
        function loadFiles() {
            fetch('/api/files')
                .then(res => res.json())
                .then(files => {
                    const filesList = document.getElementById('filesList');
                    if (files.length === 0) {
                        filesList.innerHTML = '<div class="empty-state">No files uploaded yet</div>';
                        return;
                    }
                    filesList.innerHTML = files.map(file => `
                        <div class="file-item">
                            <div class="file-info">
                                <h3>${r"${file.fileName}"}</h3>
                                <div class="file-meta">
                                    Size: ${r"${formatFileSize(file.fileSize)}"} |
                                    Uploaded: ${r"${new Date(file.uploadedAt).toLocaleString()}"}
                                </div>
                            </div>
                            <a href="/api/files/${r"${file.id}"}/download" class="download-btn">Download</a>
                        </div>
                    `).join('');
                });
        }

        function uploadFile(event) {
            event.preventDefault();
            const fileInput = document.getElementById('fileInput');
            const formData = new FormData();
            formData.append('file', fileInput.files[0]);

            fetch('/api/files/upload', {
                method: 'POST',
                body: formData
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    fileInput.value = '';
                    loadFiles();
                    alert('File uploaded successfully!');
                } else {
                    alert('Upload failed: ' + data.error);
                }
            })
            .catch(err => alert('Upload failed'));
        }

        function formatFileSize(bytes) {
            if (bytes < 1024) return bytes + ' B';
            if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
            return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
        }

        function logout() {
            window.location.href = '/api/auth/logout';
        }

        loadFiles();
    </script>
</body>
</html>

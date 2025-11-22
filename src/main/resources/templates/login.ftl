<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - File Upload App</title>
    <script src="https://accounts.google.com/gsi/client" async defer></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        .login-container {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            text-align: center;
        }
        h1 {
            color: #333;
            margin-bottom: 30px;
        }
        p {
            color: #666;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>File Upload App</h1>
        <p>Sign in with your Google account</p>
        <div id="g_id_onload"
             data-client_id="${clientId}"
             data-callback="handleCredentialResponse"></div>
        <div class="g_id_signin" data-type="standard"></div>
    </div>
    <script>
        function handleCredentialResponse(response) {
            fetch('/api/auth/google', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ credential: response.credential })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = '/files';
                } else {
                    alert('Authentication failed');
                }
            });
        }

        window.onload = function() {
            google.accounts.id.initialize({
                client_id: '${clientId}',
                callback: handleCredentialResponse
            });
            google.accounts.id.renderButton(
                document.querySelector('.g_id_signin'),
                { theme: 'outline', size: 'large' }
            );
        };
    </script>
</body>
</html>

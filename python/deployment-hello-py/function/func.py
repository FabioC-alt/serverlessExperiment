import logging
import json

class Function:
    def __init__(self):
        pass

    async def handle(self, scope, receive, send):
        logging.info("OK: Request Received")

        if scope["method"] == "POST":
            body = b""
            more_body = True
            while more_body:
                event = await receive()
                body += event.get("body", b"")
                more_body = event.get("more_body", False)

            try:
                data = json.loads(body.decode())
                name = data.get("name", "World")
                response_text = json.dumps({"message": f"Hello, {name}!"})
                status = 200
                content_type = b"application/json"
            except Exception as e:
                response_text = json.dumps({"error": str(e)})
                status = 400
                content_type = b"application/json"
        else:
            response_text = json.dumps({"error": "Only POST supported"})
            status = 405
            content_type = b"application/json"

        await send({
            'type': 'http.response.start',
            'status': status,
            'headers': [
                [b'content-type', content_type],
            ],
        })
        await send({
            'type': 'http.response.body',
            'body': response_text.encode(),
        })


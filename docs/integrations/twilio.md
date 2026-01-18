# Twilio (stub) mapping

Twilio sends webhooks as x-www-form-urlencoded fields (not JSON).

We map Twilio → canonical NotifyHub DTO:

- channel: "SMS"
- phoneNumber: From
- body: Body
- receivedAt: (not provided by Twilio webhook) -> server time

Typical Twilio fields include `From`, `To`, `Body`, `MessageSid`. 

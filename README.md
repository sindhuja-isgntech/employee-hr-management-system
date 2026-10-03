# employee-hr-management-system
Employee HR management System

## Password recovery

Password reset emails require SMTP settings in the backend environment. Configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM`. For SMTP providers that require authentication and TLS, keep `MAIL_SMTP_AUTH=true` and `MAIL_SMTP_STARTTLS=true`.

Set `APP_FRONTEND_URL` to the frontend origin used in reset links (for local development, `http://localhost:5173`). Reset links expire after 30 minutes by default; override this with `PASSWORD_RESET_EXPIRATION_MINUTES`. Do not commit SMTP credentials.

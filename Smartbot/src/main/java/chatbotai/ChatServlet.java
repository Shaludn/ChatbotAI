package chatbotai;


import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.net.*;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        BufferedReader reader = req.getReader();
        String msg = reader.readLine();

        String reply;

        if (msg == null || msg.trim().isEmpty()) {
            reply = "Empty input";
        } 
        else if (msg.matches("[0-9+\\-*/(). ]+")) {
            reply = solveMath(msg);
        } 
        else {
            reply = getAIResponse(msg);
        }

        res.setContentType("text/plain");
        res.getWriter().write(reply);
    }

    private String solveMath(String expr) {
        try {
            return String.valueOf(eval(expr));
        } catch (Exception e) {
            return "Invalid math";
        }
    }

    // Simple math parser
    private double eval(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                }

                return x;
            }
        }.parse();
    }

    // 🔥 AI (OpenRouter)
    private String getAIResponse(String msg) {

        try {

            String apiKey = "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";

            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "http://localhost:8080");
            conn.setRequestProperty("X-Title", "ChatbotAI");

            conn.setDoOutput(true);

            String jsonInput = "{"
                    + "\"model\":\"openai/gpt-3.5-turbo\","
                    + "\"messages\":["
                    + "{\"role\":\"user\",\"content\":\"" + msg + "\"}"
                    + "]"
                    + "}";
            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            BufferedReader br;

            if (conn.getResponseCode() == 200) {
                br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
            } else {

                // 🔥 THIS SHOWS REAL ERROR
                br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream())
                );
            }

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            String result = response.toString();

            // 🔥 DEBUG
            System.out.println(result);

            int start = result.indexOf("\"content\":\"") + 11;
            int end = result.indexOf("\"", start);

            if (start > 10 && end > start) {
            	return result.substring(start, end)
            	        .replace("\\n", "\n")
            	        .replace("\\\"", "\"");
            }

            return result;

        } catch (Exception e) {
            return "AI Error: " + e.getMessage();
        }
    }
}
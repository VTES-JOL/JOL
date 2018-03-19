package net.deckserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> responseJson = new HashMap<>();
        responseJson.put("authResult", String.valueOf(false));
        String responseString = objectMapper.writeValueAsString(responseJson);

        resp.setContentType("application/json");
        resp.getWriter().write(responseString);
    }


}

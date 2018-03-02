package net.deckserver.servlet;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardEntry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@WebServlet("/card")
public class CardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JolAdmin jolAdmin = JolAdmin.getInstance();
        String id = req.getParameter("id");
        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();
        Optional<CardEntry> result = Optional.ofNullable(jolAdmin.getAllCards().getCardById(id));
        String html = result
                .map(CardEntry::getFullText)
                .map(cardText -> Stream.of(cardText).collect(Collectors.joining("<br/>")))
                .orElse("Card not found");
        writer.write(html);
    }
}

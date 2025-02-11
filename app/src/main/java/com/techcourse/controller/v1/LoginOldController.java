package com.techcourse.controller.v1;

import com.techcourse.controller.UserSession;
import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.handler.asis.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginOldController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(LoginOldController.class);

    private final InMemoryUserRepository userRepository;

    public LoginOldController(final InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String execute(final HttpServletRequest req, final HttpServletResponse res) throws Exception {
        if (UserSession.isLoggedIn(req.getSession())) {
            return "redirect:/index.jsp";
        }

        return userRepository.findByAccount(req.getParameter("account"))
                .map(user -> {
                    log.info("User : {}", user);
                    return login(req, user);
                })
                .orElse("redirect:/401.jsp");
    }

    private String login(final HttpServletRequest request, final User user) {
        if (user.checkPassword(request.getParameter("password"))) {
            final var session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return "redirect:/index.jsp";
        } else {
            return "redirect:/401.jsp";
        }
    }
}

package nextstep.mvc.handler.adapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.handler.asis.Controller;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;

public class ManualHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(final Object handler) {
        return handler instanceof Controller;
    }

    @Override
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
                               final Object handler) throws Exception {
        final String viewName = ((Controller) handler).execute(request, response);
        return new ModelAndView(new JspView(viewName));
    }
}

package org.synyx.urlaubsverwaltung.restapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;


@Controller("restApiBaseController")
@RequestMapping("/api")
public class BaseController {

    private static final boolean RELATIVE_CONTEXT = true;

    @GetMapping
    public RedirectView discover() {

        return new RedirectView("/swagger-ui.html", RELATIVE_CONTEXT);
    }
}

package misows.pi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class Controller {

    @Autowired
    private PiService service;

    @RequestMapping(path = "/pi/{precision}")
    public String getPi(@PathVariable int precision){
        return service.computePi(precision);
    }

    @RequestMapping(path = "/compute/{start}/{iterations}/{precision}")
    public String computePi(@PathVariable int start,@PathVariable int iterations,@PathVariable int precision){
        return service.bellardsFormula(start,iterations,precision).toPlainString();
    }
}

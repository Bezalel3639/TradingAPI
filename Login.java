package com.bezalel.trading_api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
@Api(value = "REST Endpoints", description = "User login")
public class Login
{
    @RequestMapping(path="/validate/{email}/{password}", method = RequestMethod.GET)
    public String ValidateUser(@PathVariable String email, @PathVariable String password)
    {
        CheckUser chk = new CheckUser();
        String result = chk.ValidateUser(email, password);

        return result;
    }

    @RequestMapping(path="/register/{email}/{password}", method = RequestMethod.GET)
    public String RegisterUser(@PathVariable String email, @PathVariable String password)
    {
        CheckUser chk = new CheckUser();
        String result = chk.RegisterUser(email, password);

        return result;
    }
}

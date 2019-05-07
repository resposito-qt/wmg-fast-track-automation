package pages;

import framework.Logger;
import framework.components.PageObject;
import framework.platform.ConfigProvider;
import framework.platform.html.WebObject;
import framework.platform.web.Locator;

public class LoginPage extends PageObject {

    @Locator(main = "#username")
    protected WebObject usernameInput;

    @Locator(main = "#password")
    protected WebObject passwordInput;

    @Locator(main = "button[type='submit']")
    protected WebObject submitLogin;

    public HomePage login() {
        Logger.info("Entering credentials 'email', 'password' and submitting form");
        usernameInput.type(ConfigProvider.testUser.getUsername());
        passwordInput.type(ConfigProvider.testUser.getPassword());
        clickSubmitLoginButton();
        return new HomePage();
    }

    public void clickSubmitLoginButton() {
        Logger.info("Clicking the 'submit' button");
        submitLogin.click();
    }
}

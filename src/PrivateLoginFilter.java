import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * servlet filter implementation class PrivateLoginFilter
 */
@WebFilter(filterName = "PrivateLoginFilter", urlPatterns = "/_dashboard/*")
public class PrivateLoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/private-login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(allowedURI -> requestURI.endsWith(allowedURI)) ||
                allowedURIs.stream().anyMatch(requestURI::equals);
    }

    public void init(FilterConfig fConfig) {
        String contextPath = fConfig.getServletContext().getContextPath();
        allowedURIs.add(contextPath + "login.css");
        allowedURIs.add(contextPath + "private-login.html");
        allowedURIs.add(contextPath + "private-login.js");
        allowedURIs.add(contextPath + "api/dashboard_login");
    }

    public void destroy() {
        // ignored
    }
}
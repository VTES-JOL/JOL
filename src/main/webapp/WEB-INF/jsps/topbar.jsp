<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand-lg bg-dark px-2" id="navbar" data-bs-theme="dark">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/" onclick="return doNav('main');">
        <span id="titleLink">V:TES Online</span>
    </a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavAltMarkup"
            aria-controls="navbarNavAltMarkup" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse navbar-nav" id="navbarNavAltMarkup">
        <div id="gameButtonsNav" class="navbar-nav">
            <div class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="myGamesLink" role="button"
                   data-bs-toggle="dropdown" aria-expanded="false">My Games</a>
                <ul id="gameButtons" class="dropdown-menu" aria-labelledby="myGamesLink">
                </ul>
            </div>
        </div>
        <div id="buttons" class="navbar-nav"></div>
    </div>
    <div class="d-flex align-items-center gap-1 ms-auto">
        <span id="connectionMessage" class="navbar-text text-warning d-none small">Connection issue. Retrying...</span>
        <span id="wsStatus" class="navbar-text text-warning d-none" title="Real-time updates unavailable — using polling"><i class="bi bi-wifi-off"></i></span>
        <div id="userMenu" class="nav-item dropdown d-none">
            <a class="nav-link dropdown-toggle d-flex align-items-center gap-2 py-1 px-2 user-menu-toggle text-white"
               href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                <span id="navUserFlag"></span>
                <span id="navUserName"></span>
            </a>
            <ul class="dropdown-menu dropdown-menu-end shadow">
                <li>
                    <a class="dropdown-item" href="${pageContext.request.contextPath}/profile" onclick="doNav('profile'); return false;">
                        <i class="bi bi-person-circle me-2"></i>Profile
                    </a>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <a class="dropdown-item" href="${pageContext.request.contextPath}/help" target="_blank">
                        <i class="bi bi-question-circle me-2"></i>Help
                    </a>
                </li>
                <li>
                    <a class="dropdown-item" href="#" onclick="toggleMode(); return false;">
                        <i class="bi bi-moon me-2"></i>Dark Mode
                    </a>
                </li>
                <li id="desktopViewItem">
                    <a class="dropdown-item" href="#" onclick="toggleMobileView(event);">
                        <i class="bi bi-display me-2" id="desktopViewIcon"></i><span id="desktopViewLabel"></span>
                    </a>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                    <form method="post" action="${pageContext.request.contextPath}/logout">
                        <button type="submit" name="logout" value="logout"
                                class="dropdown-item text-danger-emphasis">
                            <i class="bi bi-box-arrow-right me-2"></i>Log Out
                        </button>
                    </form>
                </li>
            </ul>
        </div>
    </div>
</nav>

import {Component, createEffect, createSignal} from "solid-js";
import {Route, Router, Routes} from "@solidjs/router";
import {TopPage} from "./pages/TopPage";
import {createTheme, CssBaseline, ThemeProvider, useMediaQuery} from "@suid/material";
import {createCookieStorage} from "@solid-primitives/storage";
import {ApiProvider} from "./lib/ApiProvider";
import {Configuration, DefaultApi} from "./generated";
import {LoginPage} from "./pages/LoginPage";

export const App: Component = () => {
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
    const [cookie, setCookie] = createCookieStorage()
    const [api, setApi] = createSignal(new DefaultApi(new Configuration({
        basePath: window.location.origin + "/api/internal/v1",
        accessToken: cookie.token as string
    })))

    createEffect(() => {
        setApi(
            new DefaultApi(new Configuration({
                basePath: window.location.origin + "/api/internal/v1",
                accessToken : cookie.token as string
            })))
    })

    const theme = createTheme({
        palette: {
            mode: prefersDarkMode() ? 'dark' : 'light',
        }
    })
    return (
        <ApiProvider api={api()}>
            <ThemeProvider theme={theme}>
                <CssBaseline/>
                <Router>
                    <Routes>
                        <Route path="/" component={TopPage}/>
                        <Route path="/login" component={LoginPage}/>
                    </Routes>
                </Router>
            </ThemeProvider>
        </ApiProvider>
    )
}

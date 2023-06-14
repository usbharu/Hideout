import {Component} from "solid-js";
import {Route, Router, Routes} from "@solidjs/router";
import {TopPage} from "./pages/TopPage";
import {createTheme, CssBaseline, ThemeProvider, useMediaQuery} from "@suid/material";

export const App: Component = () => {
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

    const theme = createTheme({
        palette: {
            mode: prefersDarkMode() ? 'dark' : 'light',
        }
    })
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <Router>
                <Routes>
                    <Route path="/" component={TopPage}/>
                </Routes>
            </Router>
        </ThemeProvider>
    )
}
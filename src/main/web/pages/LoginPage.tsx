import {Button, Card, CardContent, CardHeader, Modal, Stack, TextField} from "@suid/material";
import {Component, createSignal} from "solid-js";
import {createCookieStorage} from "@solid-primitives/storage";
import {useApi} from "../lib/ApiProvider";

export const LoginPage: Component = () => {
    const [username, setUsername] = createSignal("")
    const [password, setPassword] = createSignal("")

    const [cookie, setCookie] = createCookieStorage();

    const api = useApi();

    const onSubmit: () => void = () => {
        api().loginPost({password: password(), username: username()}).then(value => {
            setCookie("token", value.token);
            setCookie("refresh-token", value.refreshToken)
        }).catch(reason => {
            console.log(reason);
            setPassword("")
        })
    }

    return (
        <Modal open>
            <Card>
                <CardHeader/>
                <CardContent>

                    <Stack spacing={3}>

                        <TextField
                            value={username()}
                            onChange={(event) => setUsername(event.target.value)}
                            label="Username"
                            type="text"
                            autoComplete="username"
                            variant="standard"
                        />
                        <TextField
                            value={password()}
                            onChange={(event) => setPassword(event.target.value)}
                            label="Password"
                            type="password"
                            autoComplete="current-password"
                            variant="standard"
                        />
                        <Button type={"submit"} onClick={onSubmit}>Login</Button>
                    </Stack>
                </CardContent>
            </Card>
        </Modal>
    )
}

import {Button, Card, CardContent, CardHeader, Modal, Stack, TextField} from "@suid/material";
import {Component, createSignal} from "solid-js";

export const LoginPage:Component = () => {
    const [username,setUsername] = createSignal("")
    const [password,setPassword] = createSignal("")

    return (
        <Modal open>
            <Card>
                <CardHeader/>
                <CardContent>

                    <Stack spacing={3}>

                        <TextField
                            value={username()}
                            onChange={(event)=>setUsername(event.target.value)}
                            label="Username"
                            type="text"
                            autoComplete="username"
                            variant="standard"
                        />
                        <TextField
                            value={password()}
                            onChange={(event)=>setPassword(event.target.value)}
                            label="Password"
                            type="password"
                            autoComplete="current-password"
                            variant="standard"
                        />
                        <Button type={"submit"} onClick={()=>{
                            console.log(username() +" " + password())}}>Login</Button>
                    </Stack>
                </CardContent>
            </Card>
        </Modal>
    )
}

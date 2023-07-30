import {Component, createSignal} from "solid-js";
import {Button, IconButton, Paper, Stack, TextField, Typography} from "@suid/material";
import {Avatar} from "../atoms/Avatar";
import {AddPhotoAlternate, Poll, Public} from "@suid/icons-material";
import {useApi} from "../lib/ApiProvider";

export const PostForm: Component<{ label: string }> = (props) => {
    const [text, setText] = createSignal("")
    const api = useApi()
    return (
        <Paper sx={{width: "100%"}}>
            <Stack>
                <Stack direction={"row"} spacing={2} sx={{padding: 2}}>
                    <Avatar src={""}/>
                    <TextField label={props.label} multiline rows={4} variant={"standard"} onChange={(event)=>setText(event.target.value)} fullWidth/>
                </Stack>
                <Stack direction={"row"} justifyContent={"space-between"} sx={{padding: 2}}>
                    <Stack direction={"row"} justifyContent={"flex-start"} alignItems={"center"}>
                        <IconButton>
                            <AddPhotoAlternate/>
                        </IconButton>
                        <IconButton>
                            <Poll/>
                        </IconButton>
                        <IconButton>
                            <Public/>
                        </IconButton>
                    </Stack>
                    <Stack direction={"row"} alignItems={"center"} spacing={2}>
                        <Typography>
                            aaa
                        </Typography>
                        <Button variant={"contained"} onClick={() => {
                            api().postsPost({text: text()}).then(()=>setText(""))
                        }}>
                            投稿する
                        </Button>
                    </Stack>
                </Stack>
            </Stack>
        </Paper>
    )
}

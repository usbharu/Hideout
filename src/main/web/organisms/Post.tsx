import {Component, createSignal, Match, Switch} from "solid-js";
import {PostResponse} from "../generated";
import {Box, Card, CardActions, CardContent, CardHeader, IconButton, Menu, MenuItem, Typography} from "@suid/material";
import {Avatar} from "../atoms/Avatar";
import {Favorite, Home, Lock, Mail, MoreVert, Public, Reply, ScreenRotationAlt} from "@suid/icons-material";

export const Post: Component<{ post: PostResponse }> = (props) => {
    const [anchorEl, setAnchorEl] = createSignal<null | HTMLElement>(null)
    const open = () => Boolean(anchorEl());
    const handleClose = () => {
        setAnchorEl(null);
    }

    return (
        <Card>
            <CardHeader avatar={<Avatar src={""}/>} title={"test user"} subheader={"test@test"}
                        action={<IconButton onclick={(event) => {
                            setAnchorEl(event.currentTarget)
                        }}><MoreVert/><Menu disableScrollLock anchorEl={anchorEl()} open={open()} onClose={handleClose}><MenuItem
                            onclick={handleClose}>aaa</MenuItem></Menu> </IconButton>}/>
            <CardContent>
                <Typography>
                    {props.post.text}
                </Typography>
            </CardContent>
            <CardActions disableSpacing>
                <IconButton>
                    <Reply/>
                </IconButton>
                <IconButton>
                    <ScreenRotationAlt/>
                </IconButton>
                <IconButton>
                    <Favorite/>
                </IconButton>
                <Box sx={{marginLeft: "auto"}}>
                    <Typography>{new Date(props.post.createdAt).toDateString()}</Typography>
                </Box>
                <Switch fallback={<Public/>}>
                    <Match when={props.post.visibility == "public"}>
                        <IconButton>
                            <Public/>
                        </IconButton>
                    </Match>
                    <Match when={props.post.visibility == "direct"}>
                        <IconButton>
                            <Mail/>
                        </IconButton>
                    </Match>
                    <Match when={props.post.visibility == "followers"}>
                        <IconButton>
                            <Lock/>
                        </IconButton>
                    </Match>
                    <Match when={props.post.visibility == "unlisted"}>
                        <IconButton>
                            <Home/>
                        </IconButton>
                    </Match>
                </Switch>
            </CardActions>
        </Card>
    )
}
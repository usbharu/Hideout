import {Component, createSignal} from "solid-js";
import {Box, Card, CardActions, CardContent, CardHeader, IconButton, Menu, MenuItem, Typography} from "@suid/material";
import {Avatar} from "../atoms/Avatar";
import {Favorite, MoreVert, Reply, ScreenRotationAlt} from "@suid/icons-material";
import {ShareScopeIndicator} from "../molecules/ShareScopeIndicator";
import {PostResponse} from "../generated";

export const Post: Component<{ post: PostResponse }> = (props) => {
    const [anchorEl, setAnchorEl] = createSignal<null | HTMLElement>(null)
    const open = () => Boolean(anchorEl());
    const handleClose = () => {
        setAnchorEl(null);
    }

    return (
        <Card>
            <CardHeader avatar={<Avatar src={props.post.user.url + "/icon.jpg"}/>} title={props.post.user.screenName}
                        subheader={`${props.post.user.name}@${props.post.user.domain}`}
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
                <ShareScopeIndicator visibility={props.post.visibility}/>
            </CardActions>
        </Card>
    )
}

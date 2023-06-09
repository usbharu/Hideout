import {Component, createSignal} from "solid-js";

export const App: Component = () => {

    const fn = (form: HTMLButtonElement) => {
        console.log(form)
    }

    const [username, setUsername] = createSignal("")
    const [password, setPassword] = createSignal("")

    return (
        <form onSubmit={function (e: SubmitEvent) {
            e.preventDefault()
            fetch("/login", {
                method: "POST",
                body: JSON.stringify({username: username(), password: password()}),
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then(res => res.json())
                // .then(res => fetch("/auth-check", {
                //     method: "GET",
                //     headers: {
                //         'Authorization': 'Bearer ' + res.token
                //     }
                // }))
                // .then(res => res.json())
                .then(res => {
                console.log(res.token);
                fetch("/refresh-token", {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({refreshToken: res.refreshToken}),
                }).then(res=> res.json()).then(res => console.log(res.token))
            })
        }

        }>
            <input name="username" type="text" placeholder="Username" required
                   onChange={(e) => setUsername(e.currentTarget.value)}/>
            <input name="password" type="password" placeholder="Password" required
                   onChange={(e) => setPassword(e.currentTarget.value)}/>
            <button type="submit">Submit</button>
        </form>
    )
}


declare module 'solid-js' {
    namespace JSX {
        interface Directives {
            fn: (form: HTMLFormElement) => void
        }
    }
}

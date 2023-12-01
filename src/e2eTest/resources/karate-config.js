function fn() {
    var env = karate.env; // get java system property 'karate.env'
    karate.log('karate.env system property was:', env);
    if (!env) {
        env = 'dev'; // a custom 'intelligent' default
        karate.log('karate.env set to "dev" as default.');
    }
    let config;
    if (env === 'test') {
        config = {
            baseUrl: 'https://test-hideout.usbharu.dev'
        }
    } else if (env === 'dev') {
        let port = karate.properties['karate.port'] || '8080'
        config = {
            baseUrl: 'http://localhost:' + port
        }
    } else {
        throw 'Unknown environment [' + env + '].'
    }
    // don't waste time waiting for a connection or if servers don't respond within 0,3 seconds
    karate.configure('connectTimeout', 1000);
    karate.configure('readTimeout', 1000);
    return config;
}

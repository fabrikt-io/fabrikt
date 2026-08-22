# Fabrikt Playground

The Fabrikt Playground is a web-based tool that allows you to play around with Fabrikt without installing it locally.

The goal is to lower the barrier for trying out Fabrikt and to show that it can be a useful tool, encouraging people to embed it in their development workflow either via the CLI or via Gradle/Maven.

## Technical Details

The playground is built with these amazing Open Source libraries ♥️
* [Ktor](https://github.com/ktorio/ktor) for HTTP
* [kotlinx.html](https://github.com/Kotlin/kotlinx.html) for HTML without writing HTML 
* [htmx](https://github.com/bigskysoftware/htmx) for interactivity
* [PrismJS](https://github.com/PrismJS/prism) for syntax highlighting
* [Ace Editor](https://github.com/ajaxorg/ace) for YAML editing
* [Normalize.css](https://github.com/necolas/normalize.css) for default styling
* [BassCSS](https://basscss.com/) for utility CSS

## Building and Deploying

1. Build the jar 
```shell
gradle :playground:shadowJar
```

2. Build the Docker image

```shell
docker build -t fabrikt-playground .
```

3. Deploy to the PaaS of choice :rocket:

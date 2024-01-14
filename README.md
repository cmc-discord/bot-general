# CMC Bot: General

This Discord bot provides moderation tooling and general-purpose features for the Community Management Community
Discord server.

# Development

Bot development requires the following:

- An IDE that can work with Kotlin and Gradle,
  such as [IntelliJ IDEA](https://www.jetbrains.com/idea/download/?section=windows)
  (scroll down for the community edition)
- A JDK, version 17 or later, such as [Adoptium](https://adoptium.net/)
- A MongoDB server for testing, such as [the community server](https://www.mongodb.com/try/download/community)

To test the bot, copy `.env.example` to `.env`, fill it out, and use Gradle to execute the `run` task.

For your convenience, this project includes run configurations for IntelliJ IDEA.

# Deployment

To deploy this bot, use the provided `docker-compose.yml` file, providing the following environmental variables:

- `DB_USERNAME`: Username to use for the MongoDB root account, automatically created if it doesn't exist.
- `DB_PASSWORD`: Password for the root account.
- `TOKEN`: Your Discord bot token.

You may wish to update the `volumes` section for the database container.

You can use something like [Watchtower](https://containrrr.dev/watchtower/) to keep the bot up to date.

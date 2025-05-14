# Lantern
Free, Open-Source, Peer-to-Peer multiplayer for Minecraft.
![transparente7341b_0](https://github.com/user-attachments/assets/411c266a-7fde-4cdd-bb05-0f8ba65842ed)
[See the Modrinth page!](https://modrinth.com/project/p2p-lantern)


# Disclaimer
**Do not share your public IP address to people you do not trust.** We are not liable for any damages that result.
**A lot of this code is an amalgamation of Sockets, packets, and a shitton of patched-together "solutions" to convince the game to accept the connection.**


# Explanation
In simple terms, the way Lantern works is as follows:
- As you host, your Minecraft client opens the world to LAN, creating a sort of network.
- When someone joins, it sends a request, and the server responds with a basic handshake.
- When you accept the connection, it creates a UUID and ServerPlayerEntity using the username, and sends a packet for the connecting client to join the world through the LAN server.


# Notes
- Lantern is **very early** in development. It currently has <ins>limited connction to the Minecraft client</ins>, meaning a lot of it is still work-in-progress.
- Lantern *does* have a license, and while we want you to feel safe using our mod, we also want to protect our work, and our users.


# Error Codes
Got an error code? Find out why [here](https://lantern-dev-team.github.io/LANTERN/err_codes)!

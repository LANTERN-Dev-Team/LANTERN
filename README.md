# Lantern
Free, Open-Source, Peer-to-Peer multiplayer for Minecraft.
![transparente7341b_0](https://github.com/user-attachments/assets/411c266a-7fde-4cdd-bb05-0f8ba65842ed)
[See the Modrinth page!](https://modrinth.com/project/p2p-lantern)


# Disclaimer
**Do not share your public IP address to people you do not trust.** We are not liable for any damages that result.
**A lot of this code is an amalgamation of Sockets, packets, and a shitton of patched-together "solutions" to convince the game to accept the connection.**


# Explanation
In simple terms, the way Lantern works is as follows:
- As you host, your **Minecraft client** opens the world to LAN, creating a sort of network.
- When someone joins, their **Lantern client** and the host's **Lantern server** have a short back-and-fourth:

#
- Client sends username.
- Server responds with a confirmation.
- Client sends UUID.
- Server responds with a confirmation.
- Server generates a ServerPlayerEntity, and uses the ClientConnection to create a LanternClientConnection for the player. (Which is just an extension of ClientConnection with extra utils / flexability.)
- Server sends something called a 'GameJoinS2CPacket', which essentially gives the client info on the server, world, world's seed, gamemode, is hardcore, etc, and connects the player to the LAN server Lantern created.

[This is mostly concept since the GJS2CP hasn't been tested yet, due to other issues.]


# Notes
- Lantern is **very early** in development. It currently has <ins>limited connction to the Minecraft client</ins>, meaning a lot of it is still work-in-progress.
- The "Lantern client" and "Lantern server" are completely seperate from your "Minecraft server" or "Minecraft client".
- Lantern *does* have a license, and while we want you to feel safe using our mod, we also want to protect our work, and our users.


# Error Codes
Got an error code? Find out why [here](https://lantern-dev-team.github.io/LANTERN/err_codes)!




# Issues
- CustomServer.java / 314  |  LanternClientConnection's "Socket" returns closed, and in return stops any further connection.

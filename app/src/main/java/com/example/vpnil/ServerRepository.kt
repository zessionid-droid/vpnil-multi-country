package com.example.vpnil

data class VpnServer(
    val id: String,
    val country: String,
    val city: String,
    val code: String,
    val endpoint: String,
    val clientPrivateKey: String,
    val serverPublicKey: String,
    val clientAddress: String = "10.66.66.2/32",
    val dns: String = "1.1.1.1",
    val allowedIps: String = "0.0.0.0/0"
)

object ServerRepository {
    val servers: List<VpnServer> = listOf(
        VpnServer(
            id = "sg-01",
            country = "Singapura",
            city = "Singapore",
            code = "SG",
            endpoint = "sg-node.contoh-server.com:51820",
            clientPrivateKey = "REPLACE_CLIENT_PRIVATE_KEY_SG",
            serverPublicKey = "REPLACE_SERVER_PUBLIC_KEY_SG"
        ),
        VpnServer(
            id = "jp-01",
            country = "Jepang",
            city = "Tokyo",
            code = "JP",
            endpoint = "jp-node.contoh-server.com:51820",
            clientPrivateKey = "REPLACE_CLIENT_PRIVATE_KEY_JP",
            serverPublicKey = "REPLACE_SERVER_PUBLIC_KEY_JP"
        ),
        VpnServer(
            id = "us-01",
            country = "Amerika Serikat",
            city = "Los Angeles",
            code = "US",
            endpoint = "us-node.contoh-server.com:51820",
            clientPrivateKey = "REPLACE_CLIENT_PRIVATE_KEY_US",
            serverPublicKey = "REPLACE_SERVER_PUBLIC_KEY_US"
        ),
        VpnServer(
            id = "de-01",
            country = "Jerman",
            city = "Frankfurt",
            code = "DE",
            endpoint = "de-node.contoh-server.com:51820",
            clientPrivateKey = "REPLACE_CLIENT_PRIVATE_KEY_DE",
            serverPublicKey = "REPLACE_SERVER_PUBLIC_KEY_DE"
        ),
        VpnServer(
            id = "nl-01",
            country = "Belanda",
            city = "Amsterdam",
            code = "NL",
            endpoint = "nl-node.contoh-server.com:51820",
            clientPrivateKey = "REPLACE_CLIENT_PRIVATE_KEY_NL",
            serverPublicKey = "REPLACE_SERVER_PUBLIC_KEY_NL"
        )
    )
}

package com.github.mtkw0127.scheduleshare.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.createSupabaseClient

class AuthRepository {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://wxnryzktnqvthtqvzzje.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Ind4bnJ5emt0bnF2dGh0cXZ6emplIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjkxODczMjIsImV4cCI6MjA0NDc2MzMyMn0.qzVvGhWb2cAcKKfyL1_ug2jHK3WyN3xpnEIrFfPW_hc"
    ) {
        install(Auth)
        install(ComposeAuth)
    }
}
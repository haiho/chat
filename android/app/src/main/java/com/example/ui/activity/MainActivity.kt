package com.example.ui.activity

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.animation.Crossfade
import androidx.ui.core.setContent
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import chat.Chat
import com.example.application.MyApplication
import com.example.data.DataStore
import com.example.db.UserRepository
import com.example.model.Room
import com.example.ui.ChatStatus
import com.example.ui.Screen
import com.example.ui.home.*
import grpc.PscrudGrpc
import grpc.PscrudOuterClass
import io.grpc.stub.StreamObserver

class MainActivity : AppCompatActivity() {

    lateinit var grpcClient: PscrudGrpc.PscrudStub
    lateinit var mainThreadHandler: Handler
    lateinit var dbLocal: UserRepository
    val rooms = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as MyApplication).container
        grpcClient = appContainer.grpcClient
        mainThreadHandler = appContainer.mainThreadHandler
        dbLocal = appContainer.dbLocal
        listen()
        subscribe()
        setContent {
            DrawerAppComponent()
        }
    }

    @Composable
    fun DrawerAppComponent() {
        Crossfade(ChatStatus.currentScreen) { screen ->
            Surface(color = MaterialTheme.colors.background) {
                when (screen) {
                    is Screen.Home -> HomeView(rooms, this, dbLocal, grpcClient, mainThreadHandler)
                    is Screen.HomeView2 -> HomeView2(this, dbLocal, grpcClient, mainThreadHandler)
                    is Screen.CreateNewRoom -> CreateNewRoom(rooms)
                    is Screen.RoomDetail -> RoomDetail(screen.roomId, grpcClient, mainThreadHandler)
                }
            }
        }
    }

    // subcribe topic
    private fun subscribe() {
        val request = PscrudOuterClass.SubscribeRequest.newBuilder()
            .setTopic(DataStore.username)
            .setSession(DataStore.session)
            .build()

        grpcClient.subscribe(request, object : StreamObserver<PscrudOuterClass.Response> {
            override fun onNext(response: PscrudOuterClass.Response?) {
                response?.ok?.let { isSuccessful ->
                }
            }

            override fun onError(t: Throwable?) {
            }

            override fun onCompleted() {
            }
        })
    }

    // Listener message from sender
    private fun listen() {
        val request = PscrudOuterClass.Request.newBuilder()
            .setSession(DataStore.session)
            .build()
        grpcClient.listen(request, object : StreamObserver<PscrudOuterClass.Publication> {
            override fun onNext(value: PscrudOuterClass.Publication?) {
                if (null != value) {
                    hear(
                        value.id,
                        Chat.Chit.parseFrom(value.data),
                        grpcClient,
                        mainThreadHandler,
                        dbLocal
                    )
                }
            }

            override fun onError(t: Throwable?) {
            }

            override fun onCompleted() {
            }
        })
    }
}


<template>
  <div>
    <h1>Chat App</h1>
    <div>
      Current session: {{ clientId }}
    </div>
    <div>
      Current game ID: {{ game.gameId }}, balls: {{ game.balls.length }}, players: {{ game.clients }}
    </div>
    <div v-for="(message, index) in messages" :key="index">
      {{ message }}
    </div>
    <Ball v-for="ball in game.balls" :key="ball.ballId" :color="ball.color" />
    <form @submit.prevent="sendMessage">
      <input v-model="messageText" type="text" placeholder="Type your message here">
      <button type="submit">Send</button>
    </form>
    <form @submit.prevent="createGame">
      <button type="submit">Create Game</button>
    </form>
    <form @submit.prevent="joinGame">
      <input v-model="gameJoinInput" type="text" placeholder="Type your message here">
      <button type="submit">Join Game</button>
    </form>
    <form @submit.prevent="playRed">
      <input v-model="playBallIdStr" type="text" placeholder="Type your message here">
      <button type="submit">Make red</button>
    </form>
    <form @submit.prevent="playBlue">
      <input v-model="playBallIdStr" type="text" placeholder="Type your message here">
      <button type="submit">Make blue</button>
    </form>
    <form @submit.prevent="playGreen">
      <input v-model="playBallIdStr" type="text" placeholder="Type your message here">
      <button type="submit">Make green</button>
    </form>
  </div>
</template>

<script>
import Ball from './Ball.vue';

export default {
  components: {
    Ball
  },
  data() {
    return {
      ws: null,
      messageText: '',
      gameJoinInput: '',
      messages: [],
      clientId: '',
      game: {},
      playBallIdStr: '0',
    };
  },
  mounted() {
    this.connect();
  },
  methods: {
    connect() {
      this.ws = new WebSocket('ws://localhost:3000/chat');
      console.log('WebSocket connecting...');
      this.ws.onopen = () => {
        console.log('WebSocket connected');
      };
      this.ws.onmessage = event => {
        const response = JSON.parse(event.data);
        console.log('WebSocket message received:', response);

        if (response.method === 'connect') {
          this.clientId = response.clientId;
        } else if (response.method === 'create') {
          this.game = response.game;
        } else if (response.method === 'chat') {
          this.messages.push(`${response.message.sender}: ${response.message.content}`);
        } else if (response.method === 'join') {
          this.game = response.game;
        } else if (response.method === 'update') {
          this.game = response.game;
        } else {
          this.messages.push("Unknown message: " + JSON.stringify(response,null, 2));
        }

        // this.messages.push(JSON.stringify(message,null, 2));
      };
      this.ws.onclose = () => {
        console.log('WebSocket disconnected');
        this.connect();
      };
    },
    sendMessage() {
      if (!this.messageText) {
        return;
      }
      this.ws.send(JSON.stringify({
        method: 'chat',
        sender: 'Client',
        content: this.messageText,
        timestamp: new Date().toLocaleTimeString(),
      }));
      this.messageText = '';
    },
    createGame() {
      this.ws.send(JSON.stringify({
        method: 'create',
        clientId: this.clientId,
      }));
    },
    joinGame() {
      this.ws.send(JSON.stringify({
        method: 'join',
        clientId: this.clientId,
        gameId: this.gameJoinInput,
      }));
    },
    playRed() {
      this.ws.send(JSON.stringify({
        method: 'play',
        clientId: this.clientId,
        gameId: this.game.gameId,
        ball: {
          ballId: parseInt(this.playBallIdStr),
          color: 'red',
        }
      }));
    },
    playBlue() {
      this.ws.send(JSON.stringify({
        method: 'play',
        clientId: this.clientId,
        gameId: this.game.gameId,
        ball: {
          ballId: parseInt(this.playBallIdStr),
          color: 'blue',
        }
      }));
    },
    playGreen() {
      this.ws.send(JSON.stringify({
        method: 'play',
        clientId: this.clientId,
        gameId: this.game.gameId,
        ball: {
          ballId: parseInt(this.playBallIdStr),
          color: 'green',
        }
      }));
    },
  },
};
</script>

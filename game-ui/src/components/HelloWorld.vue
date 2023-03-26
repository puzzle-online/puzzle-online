<template>
  <div>
    <h1>Chat App</h1>
    <div>
      Current session: {{ clientId }}
    </div>
    <div>
      Current game ID: {{ game.gameId }}, balls: {{ game.balls }}, players: {{ game.clients }}
    </div>
    <div v-for="(message, index) in messages" :key="index">
      {{ message }}
    </div>
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
  </div>
</template>

<script>
export default {
  data() {
    return {
      ws: null,
      messageText: '',
      gameJoinInput: '',
      messages: [],
      clientId: '',
      game: {},
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
        const transfer = JSON.parse(event.data);
        console.log('WebSocket message received:', transfer);

        if (transfer.method === 'connect') {
          this.clientId = transfer.clientId;
        } else if (transfer.method === 'create') {
          this.game = transfer.game;
        } else if (transfer.method === 'chat') {
          this.messages.push(`${transfer.message.sender}: ${transfer.message.content}`);
        } else if (transfer.method === 'join') {
          this.game = transfer.game;
        } else {
          this.messages.push("Unknown message: " + JSON.stringify(transfer,null, 2));
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
  },
};
</script>

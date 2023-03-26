<template>
  <div>
    <h1>Chat App</h1>
    <div>
      Current session: {{ clientId }}
    </div>
    <div>
      Current game ID: {{ game.gameId }}, balls: {{ game.balls }}
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
  </div>
</template>

<script>
export default {
  data() {
    return {
      ws: null,
      messageText: '',
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
        const message = JSON.parse(event.data);
        console.log('WebSocket message received:', message);

        if (message.method === 'connect') {
          this.clientId = message.clientId;
        } else if (message.method === 'create') {
          this.game = message.game;
        } else {
          this.messages.push("Unknown message: " + JSON.stringify(message,null, 2));
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
  },
};
</script>

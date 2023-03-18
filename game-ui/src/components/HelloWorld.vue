<template>
  <div>
    <h1>Chat App</h1>
    <div v-for="(message, index) in messages" :key="index">
      <strong>{{ message.sender }}</strong>: {{ message.content }} ({{ message.timestamp }})
    </div>
    <form @submit.prevent="sendMessage">
      <input v-model="messageText" type="text" placeholder="Type your message here">
      <button type="submit">Send</button>
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
    };
  },
  mounted() {
    this.connect();
  },
  methods: {
    connect() {
      this.ws = new WebSocket('ws://localhost:8000/chat');
      console.log('WebSocket connecting...');
      this.ws.onopen = () => {
        console.log('WebSocket connected');
      };
      this.ws.onmessage = event => {
        const message = JSON.parse(event.data);
        console.log('WebSocket message received:', message)
        this.messages.push(message);
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
        sender: 'Client',
        content: this.messageText,
        timestamp: new Date().toLocaleTimeString(),
      }));
      this.messageText = '';
    },
  },
};
</script>

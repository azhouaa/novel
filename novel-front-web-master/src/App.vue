<template>
  <div id="app">
    <router-view />
    <FloatingAiChat v-if="showAiWidget" />
  </div>
</template>

<script>
import { computed } from "vue";
import { useRoute } from "vue-router";
import FloatingAiChat from "@/components/common/FloatingAiChat.vue";

export default  {
  name: "App",
  components: {
    FloatingAiChat,
  },
  setup() {
    const route = useRoute();
    const showAiWidget = computed(() => !route.path.startsWith("/author"));
    return {
      showAiWidget,
    };
  },
}
</script>

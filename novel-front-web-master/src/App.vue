<template>
  <div id="app">
    <router-view />
    <SideTagCloud v-if="showTagCloud" />
    <ViewActionButtons :show-refresh="showTagCloud" />
    <FloatingAiChat v-if="showAiWidget" />
  </div>
</template>

<script>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import FloatingAiChat from "@/components/common/FloatingAiChat.vue";
import SideTagCloud from "@/components/common/SideTagCloud.vue";
import ViewActionButtons from "@/components/common/ViewActionButtons.vue";
import { applyThemeMode, getThemeMode } from "@/utils/theme";

export default {
  name: "App",
  components: {
    SideTagCloud,
    ViewActionButtons,
    FloatingAiChat,
  },
  setup() {
    const route = useRoute();
    // 作者后台页面保留现状，不展示前台AI悬浮入口。
    const showAiWidget = computed(() => !route.path.startsWith("/author"));
    // 仅在首页/分类/榜单/详情展示标签云，阅读页不展示。
    const showTagCloud = computed(() => {
      return ["home", "bookclass", "bookRank", "book"].includes(route.name);
    });

    onMounted(() => {
      // 首屏按本地设置恢复主题。
      applyThemeMode(getThemeMode());
    });

    return {
      showAiWidget,
      showTagCloud,
    };
  },
};
</script>

<style>
/* 夜间模式：统一页面底色和文字色，减少高亮眩光。 */
body.theme-night {
  background: #0f1728;
  color: #dbe4f3;
}

body.theme-night #app {
  background: #0f1728;
}

body.theme-night .topMain,
body.theme-night .mainNav {
  background: #101c31;
}

body.theme-night .main,
body.theme-night .channelWrap,
body.theme-night .wrap_bg,
body.theme-night .wrap_inner,
body.theme-night .userBox,
body.theme-night .my_r,
body.theme-night .reply_bar,
body.theme-night .bookNav,
body.theme-night .nextPageBox a,
body.theme-night .updateTable th,
body.theme-night .updateTable td,
body.theme-night .rankTable tr:nth-child(2n) td,
body.theme-night .so_tag li,
body.theme-night .textbox {
  background-color: rgba(24, 34, 53, 0.9) !important;
  color: #d5ddef !important;
}

body.theme-night a {
  color: #9eb6de;
}

body.theme-night .book_info .author,
body.theme-night .textinfo,
body.theme-night .pl_bar .other,
body.theme-night .updateTable,
body.theme-night .so_tag li .tit {
  color: #acb8cf !important;
}

body.theme-night .items_txt .intro a,
body.theme-night .rightList .book_name a,
body.theme-night .hot_articles dd a {
  color: #9fb0ca !important;
}

body.theme-night .items_txt h4 a:hover,
body.theme-night .rightList .book_name a:hover,
body.theme-night .hot_articles dd a:hover {
  color: #c3d2ea !important;
}

#app {
  position: relative;
}
</style>

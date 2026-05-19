<template>
  <div class="header">
    <div class="mainNav" id="mainNav">
      <div class="box_center cf author-bar">
        <div class="author-title">作家专区</div>
        <div class="author-welcome">您好，作家{{ displayName }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { getNickName } from "@/utils/auth";
import { getUserinfo } from "@/api/user";

export default {
  name: "AuthorHeader",
  setup() {
    const displayName = ref(getNickName() || "用户");

    const loadDisplayName = async () => {
      try {
        const { data } = await getUserinfo();
        const penName = data?.penName;
        if (penName && String(penName).trim()) {
          displayName.value = String(penName).trim();
          return;
        }
      } catch (e) {
        // 仅用于展示，接口异常时保持昵称回退。
      }
      displayName.value = getNickName() || "用户";
    };

    onMounted(() => {
      loadDisplayName();
    });

    return { displayName };
  },
};
</script>

<style scoped>
.author-bar {
  position: relative;
  display: flex;
  align-items: center;
  height: 44px;
  line-height: 44px;
  color: #fff;
}

.author-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 16px;
}

.author-welcome {
  margin-left: auto;
  margin-right: 4px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}
</style>

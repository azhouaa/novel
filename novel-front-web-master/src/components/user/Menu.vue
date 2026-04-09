<template>
  <div class="user-menu-bar">
    <router-link :class="routeName === 'userSetup' ? 'menu-btn on' : 'menu-btn'" :to="{ name: 'userSetup' }">
      账号设置
    </router-link>
    <router-link :class="routeName === 'userBookshelf' ? 'menu-btn on' : 'menu-btn'" :to="{ name: 'userBookshelf' }">
      我的书架
    </router-link>
    <router-link :class="routeName === 'userComment' ? 'menu-btn on' : 'menu-btn'" :to="{ name: 'userComment' }">
      我的评论
    </router-link>
  </div>
</template>

<script>
import { computed } from "vue";
import { useRoute } from "vue-router";

export default {
  name: "UserMenu",
  setup() {
    const route = useRoute();
    /**
     * 使用计算属性保持菜单高亮与当前路由实时同步。
     */
    const routeName = computed(() => route.name);
    return {
      routeName,
    };
  },
};
</script>

<style scoped>
.user-menu-bar {
  display: flex;
  gap: 12px;
  padding: 14px 0 18px;
}

.menu-btn {
  min-width: 136px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  border-radius: 8px;
  border: 1px solid #d8e6f6;
  color: #59708f;
  background: #f7fbff;
  transition: all 0.2s ease;
}

.menu-btn:hover {
  border-color: #8fb5dc;
  color: #2f5f93;
  background: #f0f7ff;
}

.menu-btn.on {
  color: #ffffff;
  border-color: #3f98ea;
  background: linear-gradient(120deg, #2f8fe5 0%, #56b6ff 100%);
  box-shadow: 0 6px 14px rgba(63, 152, 234, 0.24);
}

@media (max-width: 768px) {
  .user-menu-bar {
    flex-wrap: wrap;
  }
}
</style>

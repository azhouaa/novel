<template>
  <div class="mainNav" id="mainNav">
    <div class="box_center cf">
      <ul class="nav" id="navModule">
        <li><router-link :to="{ name: 'home' }">首页</router-link></li>
        <li>
          <router-link :to="{ name: 'bookclass' }"> 全部作品 </router-link>
        </li>
        <li><router-link :to="{ name: 'bookRank' }">排行榜</router-link></li>
        <li><a @click="goBookshelf" href="javascript:void(0)">我的书架</a></li>
        <li v-if="showAuthorEntry"><a @click="goAuthor" href="javascript:void(0)">作家专区</a></li>
        <li v-if="showAdminEntry"><a @click="goAdmin" href="javascript:void(0)">管理后台</a></li>
      </ul>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getToken } from "@/utils/auth";
import { getUserinfo } from "@/api/user";

export default {
  name: "CommonNavbar",
  setup() {
    const router = useRouter();
    const state = reactive({
      showAuthorEntry: false,
      showAdminEntry: false,
    });

    onMounted(async () => {
      if (!getToken()) {
        return;
      }
      try {
        const { data } = await getUserinfo();
        // 作家入口展示同时兼容“审核通过作家”与“已开通上传权限”的账号。
        state.showAuthorEntry =
          (Number(data?.isAuthor) === 1 && Number(data?.authorStatus) === 0) ||
          Number(data?.canUploadNovel) === 1;
        state.showAdminEntry = Number(data?.isAdmin) === 1;
      } catch (e) {
        state.showAuthorEntry = false;
        state.showAdminEntry = false;
      }
    });

    /**
     * 进入书架页，未登录先跳转登录页。
     */
    const goBookshelf = async () => {
      if (!getToken()) {
        router.push({ name: "login" });
        return;
      }
      router.push({ name: "userBookshelf" });
    };

    /**
     * 进入作家专区。
     */
    const goAuthor = async () => {
      if (!getToken()) {
        router.push({ name: "login" });
        return;
      }
      router.push({ name: "authorBookList" });
    };

    /**
     * 进入管理员账号管理页。
     */
    const goAdmin = async () => {
      if (!getToken()) {
        router.push({ name: "login" });
        return;
      }
      router.push({ name: "adminUserManage" });
    };

    return {
      ...toRefs(state),
      goBookshelf,
      goAuthor,
      goAdmin,
    };
  },
};
</script>

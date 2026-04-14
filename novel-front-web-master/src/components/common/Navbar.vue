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
        <li><a @click="goAuthor" href="javascript:void(0)">作家专区</a></li>
        <li><a @click="goUploadNovel" href="javascript:void(0)">上传小说</a></li>
      </ul>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router";
import { getToken } from "@/utils/auth";
import { getAuthorStatus } from "@/api/author";

export default {
  name: "CommonNavbar",
  setup() {
    const router = useRouter();

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
     * 进入作家专区，若未注册作者先跳转作者注册页。
     */
    const goAuthor = async () => {
      if (!getToken()) {
        router.push({ name: "login" });
        return;
      }
      const { data } = await getAuthorStatus();
      if (data === null) {
        router.push({ name: "authorRegister" });
        return;
      }
      const routeUrl = router.resolve({ name: "authorBookList" });
      window.open(routeUrl.href, "_blank");
    };

    /**
     * 进入上传小说页，复用作者身份校验逻辑。
     */
    const goUploadNovel = async () => {
      if (!getToken()) {
        router.push({ name: "login" });
        return;
      }
      const { data } = await getAuthorStatus();
      if (data === null) {
        router.push({ name: "authorRegister" });
        return;
      }
      const routeUrl = router.resolve({ name: "authorBookUpload" });
      window.open(routeUrl.href, "_blank");
    };

    return {
      goBookshelf,
      goAuthor,
      goUploadNovel,
    };
  },
};
</script>

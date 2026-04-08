<template>
  <Header />

  <div class="main box_center cf">
    <div class="userBox cf">
      <UserMenu />
      <div class="my_r">
        <div class="my_info cf">
          <div class="my_info_txt">
            <ul class="mytab_list">
              <li>
                <i class="tit">我的头像</i>
                <el-upload
                  class="avatar-uploader"
                  :action="baseUrl + '/front/resource/image'"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <img :src="userPhoto ? imgBaseUrl + userPhoto : man" class="avatar" />
                </el-upload>
              </li>
              <li>
                <i class="tit">我的昵称</i>
                <a id="my_name">{{ nickName }}</a>
              </li>
              <li>
                <i class="tit">阅读喜好</i>
                <div class="prefer-tags-panel">
                  <a
                    v-for="tag in preferOptions"
                    :key="tag"
                    href="javascript:void(0)"
                    :class="['prefer-tag-item', { on: isTagActive(tag) }]"
                    @click="toggleTag(tag)"
                  >
                    {{ tag }}
                  </a>
                  <a class="save-prefer-btn" href="javascript:void(0)" @click="savePreferTags">
                    保存喜好
                  </a>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/user.css";
import man from "@/assets/images/man.png";
import { reactive, toRefs, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { listCategorys } from "@/api/book";
import { getUserinfo, updateUserInfo } from "@/api/user";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import UserMenu from "@/components/user/Menu";

export default {
  name: "userSetup",
  components: {
    Header,
    Footer,
    UserMenu,
  },
  setup() {
    const state = reactive({
      userPhoto: "",
      nickName: "",
      preferTags: "",
      preferOptions: [],
      baseUrl: process.env.VUE_APP_BASE_API_URL,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(async () => {
      const { data } = await getUserinfo();
      state.userPhoto = data.userPhoto;
      state.nickName = data.nickName;
      state.preferTags = data.preferTags || "";
      await loadPreferOptions();
    });

    const loadPreferOptions = async () => {
      const [maleResp, femaleResp] = await Promise.all([
        listCategorys({ workDirection: 0 }),
        listCategorys({ workDirection: 1 }),
      ]);
      const tags = ["男频"]
        .concat((maleResp.data || []).map((item) => item.name))
        .concat((femaleResp.data || []).map((item) => (item.name === "女生频道" ? "女频" : item.name)));
      state.preferOptions = [...new Set(tags)];
    };

    const parsePreferTags = () => {
      if (!state.preferTags) {
        return [];
      }
      return state.preferTags.split(",").map((item) => item.trim()).filter(Boolean);
    };

    const isTagActive = (tag) => parsePreferTags().includes(tag);

    const toggleTag = (tag) => {
      const tags = parsePreferTags();
      const idx = tags.indexOf(tag);
      if (idx >= 0) {
        tags.splice(idx, 1);
      } else {
        tags.push(tag);
      }
      state.preferTags = tags.join(",");
    };

    const savePreferTags = async () => {
      await updateUserInfo({ preferTags: state.preferTags });
      ElMessage.success("喜好标签已保存");
    };

    const beforeAvatarUpload = (rawFile) => {
      if (rawFile.type !== "image/jpeg") {
        ElMessage.error("必须上传 JPG 格式的图片");
        return false;
      }
      if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error("图片大小最大 5MB");
        return false;
      }
      return true;
    };

    const handleAvatarSuccess = (response) => {
      state.userPhoto = response.data;
      updateUserInfo({ userPhoto: state.userPhoto });
    };

    return {
      ...toRefs(state),
      man,
      beforeAvatarUpload,
      handleAvatarSuccess,
      isTagActive,
      toggleTag,
      savePreferTags,
    };
  },
};
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.prefer-tags-panel {
  display: inline-block;
  width: 560px;
}

.prefer-tag-item {
  display: inline-block;
  margin: 0 8px 8px 0;
  padding: 2px 10px;
  border: 1px solid #d9e2f0;
  border-radius: 14px;
  color: #556070;
}

.prefer-tag-item.on {
  border-color: #2196f3;
  color: #2196f3;
}

.save-prefer-btn {
  display: inline-block;
  margin-left: 4px;
  color: #2196f3;
}
</style>

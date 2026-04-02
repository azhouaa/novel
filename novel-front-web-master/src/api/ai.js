import request from "@/utils/request";

export function chatWithAi(data) {
  return request.post("/front/ai/chat", data);
}

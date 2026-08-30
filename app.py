
import streamlit as st
from llama_cpp import Llama

st.set_page_config(page_title="Naina AI", layout="centered")
st.title("🤖 Naina AI Assistant")

# Model path (Make sure .gguf file is in same folder)
MODEL_PATH = "llama-3.2-1b-abliterated.Q4_K_M.gguf"

@st.cache_resource
def load_llm():
    return Llama(
        model_path=MODEL_PATH,
        n_ctx=2048,
        n_threads=4
    )

try:
    llm = load_llm()
except Exception as e:
    st.error(f"Model load nahi hua: {e}")
    st.stop()

if "messages" not in st.session_state:
    st.session_state.messages = []

for msg in st.session_state.messages:
    with st.chat_message(msg["role"]):
        st.markdown(msg["content"])

if prompt := st.chat_input("Apna sawal puchiye..."):
    st.session_state.messages.append({"role": "user", "content": prompt})
    with st.chat_message("user"):
        st.markdown(prompt)

    with st.chat_message("assistant"):
        with st.spinner("Soch raha hu..."):
            response = llm.create_chat_completion(
                messages=st.session_state.messages,
                max_tokens=512,
                temperature=0.7
            )
            reply = response["choices"][0]["message"]["content"]
            st.markdown(reply)
            st.session_state.messages.append({"role": "assistant", "content": reply})

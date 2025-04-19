import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/main.scss';
import 'bootstrap-icons/font/bootstrap-icons.css';
import { store } from './app/store';
import { Provider } from 'react-redux';
import i18n from './i18n';
import 'react-calendar/dist/Calendar.css';

(window as any).i18next = i18n;

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </React.StrictMode>
);
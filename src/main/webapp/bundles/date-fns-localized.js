import { setLocale } from '../lib/date-fns/locale-resolver'

if (window.navigator.language === 'de') {
  import(/* webpackChunkName: "date-fn-locale-de" */'date-fns/locale/de').then(setLocale);
}


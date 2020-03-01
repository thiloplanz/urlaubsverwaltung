import $ from 'jquery';
import 'bootstrap/js/tooltip';

function yados_fix_tooltip_show() {
  const td = $(this).closest('td')[0]
  if (td){
    td.style.overflow = "visible"
  }
}

function yados_fix_tooltip_hide() {
   const td = $(this).closest('td')[0]
   if (td){
     td.style.overflow = ""
   }
}


export default function tooltip() {
  $('[data-title]').attr('data-placement', 'bottom')
    .tooltip()
    .on('hidden.bs.tooltip', yados_fix_tooltip_hide)
    .on('inserted.bs.tooltip', yados_fix_tooltip_show)

}

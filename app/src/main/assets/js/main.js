$( document ).ready(function() {
    console.log( "ready!" );
    // вешаем обработчик на кнопку
    $('#btnPlay').click(function(){
    console.log( "onclick!" );
    // начать повторы функции function()  с интервалом 0.1 сек
    var timerId = setInterval(function() {
	// меняем картинку	
    var img = document.getElementById("myImg");
            img.src = '/img/es.html' + Math.random();
    }, 100);

    // через 500 сек(8,33 мин) остановить повторы
    setTimeout(function() {
      clearInterval(timerId);
      alert( 'стоп' );
    }, 500000);
   



    });
});


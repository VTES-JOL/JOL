/**
 * Created by shannon on 14/02/17.
 */
var deadline = 'February 14 2017 21:00 GMT+1000';

function getTimeRemaining(endtime) {
    var t = Date.parse(endtime) - Date.parse(new Date());
    var seconds = Math.floor((t / 1000) % 60);
    var minutes = Math.floor((t / 1000 / 60) % 60);
    var hours = Math.floor((t / (1000 * 60 * 60)) % 24);
    var days = Math.floor(t / (1000 * 60 * 60 * 24));
    return {
        'total': t,
        'days': days,
        'hours': hours,
        'minutes': minutes,
        'seconds': seconds
    };
}

function initializeClock(id, endtime) {
    var clock = document.getElementById(id);
    var timeInterval = setInterval(function () {
        var t = getTimeRemaining(endtime);
        clock.innerHTML = 'System restart in ' + t.days + 'd ' +
            t.hours + 'h ' +
            t.minutes + 'm ' +
            t.seconds + 's';
        if (t.total <= 0) {
            clearInterval(timeInterval);
        }
    }, 1000);
}
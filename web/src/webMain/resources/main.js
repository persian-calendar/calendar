(function () {
	'use strict';

	var CALENDARS = [
		{ id: 'civil', label: 'میلادی' },
		{ id: 'persian', label: 'شمسی' },
		{ id: 'islamic', label: 'قمری' },
	];

	var WEEKDAYS = [
		'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنج‌شنبه', 'جمعه', 'شنبه', 'یکشنبه',
	];

	var CIVIL_MONTHS = [
		'ژانویه', 'فوریه', 'مارس', 'آوریل', 'مه', 'ژوئن',
		'ژوئیه', 'اوت', 'سپتامبر', 'اکتبر', 'نوامبر', 'دسامبر',
	];

	var PERSIAN_MONTHS = [
		'فروردین', 'اردیبهشت', 'خرداد', 'تیر', 'مرداد', 'شهریور',
		'مهر', 'آبان', 'آذر', 'دی', 'بهمن', 'اسفند',
	];

	// Old-era (Borji) months, used by the Persian calendar before year 1304.
	var BORJI_MONTHS = [
		'حمل', 'ثور', 'جوزا', 'سرطان', 'اسد', 'سنبله',
		'میزان', 'عقرب', 'قوس', 'جدی', 'دلو', 'حوت',
	];

	var ISLAMIC_MONTHS = [
		'محرم', 'صفر', 'ربیع‌الاول', 'ربیع‌الثانی', 'جمادی‌الاول', 'جمادی‌الثانی',
		'رجب', 'شعبان', 'رمضان', 'شوال', 'ذی‌القعده', 'ذی‌الحجه',
	];

	var BORJI_UNTIL_YEAR = 1304;

	function monthNames(calendar, year) {
		if (calendar === 'civil') return CIVIL_MONTHS;
		if (calendar === 'islamic') return ISLAMIC_MONTHS;
		return year !== null && year < BORJI_UNTIL_YEAR ? BORJI_MONTHS : PERSIAN_MONTHS;
	}

	function monthLabel(name, month) {
		return name + ' (' + month.toLocaleString('fa') + ')';
	}

	// The year field accepts different kinds of digits alike
	function normalizeDigits(input) {
		return String(input).replace(/[۰-۹٠-٩]/g, function (digit) {
			var code = digit.charCodeAt(0);
			// ۰ is U+06F0 and ٠ is U+0660; both count up to ۹ / ٩ the same way
			return String(code <= 0x06F9 ? code - 0x06F0 : code - 0x0660);
		});
	}

	function option(value, label) {
		var created = document.createElement('option');
		created.value = value;
		created.textContent = label;
		return created;
	}

	function start(jdnOf, dateOf, daysInMonth) {
		var calendarSelect = document.getElementById('calendar');
		var yearInput = document.getElementById('year');
		var monthSelect = document.getElementById('month');
		var daySelect = document.getElementById('day');
		var output = document.getElementById('output');
		var error = document.getElementById('error');
		var diff = document.getElementById('diff');
		var weekday = document.getElementById('weekday');

		CALENDARS.forEach(function (calendar) {
			calendarSelect.appendChild(option(calendar.id, calendar.label));
		});
		var cards = CALENDARS.map(function (calendar) {
			var li = document.createElement('li');
			var day = document.createElement('div');
			day.className = 'day';
			li.appendChild(day);
			var month = document.createElement('div');
			month.className = 'month';
			li.appendChild(month);
			var date = document.createElement('div');
			date.className = 'date';
			li.appendChild(date);
			document.getElementById('cards').appendChild(li);
			return { calendar: calendar, day: day, month: month, date: date, root: li };
		});
		calendarSelect.value = 'persian';

		function todayJdn() {
			var now = new Date();
			return jdnOf('civil', now.getFullYear(), now.getMonth() + 1, now.getDate());
		}

		function enteredYear() {
			var year = +normalizeDigits(yearInput.value);
			return isNaN(year) ? null : year;
		}

		function populateMonths() {
			var names = monthNames(calendarSelect.value, enteredYear());
			var previous = +monthSelect.value;
			monthSelect.innerHTML = '';
			names.forEach(function (name, month) {
				monthSelect.appendChild(option(month + 1, monthLabel(name, month + 1)));
			});
			monthSelect.value = isNaN(previous) ? '1' : String(previous);
		}

		function populateDays() {
			var year = enteredYear();
			var month = +monthSelect.value;
			if (year === null || isNaN(month)) {
				daySelect.innerHTML = '';
				return;
			}
			var previous = +daySelect.value;
			var maxDay = daysInMonth(calendarSelect.value, year, month);
			daySelect.innerHTML = '';
			for (var day = 1; day <= maxDay; ++day) {
				daySelect.appendChild(option(day, day.toLocaleString('fa')));
			}
			daySelect.value = (previous >= 1 && previous <= maxDay) ? String(previous) : '1';
		}

		function refresh() {
			var year = enteredYear();
			var month = +monthSelect.value;
			var day = +daySelect.value;
			var usable = year !== null && year >= -10000 && year <= 10000 &&
				month >= 1 && month <= 12 && day >= 1 && day <= 32;

			if (!usable) {
				output.style.display = 'none';
				error.style.display = 'block';
				return;
			}
			output.style.display = 'block';
			error.style.display = 'none';

			var jdn = jdnOf(calendarSelect.value, year, month, day);
			var delta = jdn - todayJdn();
			diff.textContent = delta === 0
				? 'امروز'
				: (delta > 0
					? delta.toLocaleString('fa') + ' روز بعد از امروز'
					: (-delta).toLocaleString('fa') + ' روز قبل از امروز');
			weekday.textContent = WEEKDAYS[jdn % 7];

			cards.forEach(function (x) {
				var year = dateOf(x.calendar.id, jdn)[0];
				var month = dateOf(x.calendar.id, jdn)[1];
				var day = dateOf(x.calendar.id, jdn)[2];
				var dayText = day.toLocaleString('fa');
				var monthText = monthLabel(monthNames(x.calendar.id, year)[month - 1], month);
				x.root.dataset.copy = dayText + ' ' + monthText + ' ' +
					year.toLocaleString('fa', { useGrouping: false });
				x.day.textContent = dayText;
				x.month.textContent = monthText;
				x.date.textContent = [
					year.toLocaleString('fa', { useGrouping: false }),
					month.toLocaleString('fa'),
					day.toLocaleString('fa'),
				].join('/');
				x.root.classList.toggle('is-visible', x.calendar.id !== calendarSelect.value);
			});
		}

		// Tapping a card copies that date. One listener, so the markup stays a string.
		output.addEventListener('click', function (event) {
			var card = event.target.closest ? event.target.closest('li[data-copy]') : null;
			if (!card) return;
			navigator.clipboard.writeText(card.dataset.copy).then(function () {
				card.className = 'copied';
				setTimeout(function () {
					card.className = '';
				}, 800);
			}, function (error) {
				console.error('Failed to copy text: ', error);
			});
		});

		// Fills the three inputs with a date in the selected calendar.
		function setFields(jdn) {
			yearInput.value = dateOf(calendarSelect.value, jdn)[0].toLocaleString('fa', { useGrouping: false });
			populateMonths();
			monthSelect.value = String(dateOf(calendarSelect.value, jdn)[1]);
			populateDays();
			daySelect.value = String(dateOf(calendarSelect.value, jdn)[2]);
		}

		// Changing the input calendar keeps the same instant, so the date is converted
		// from the calendar it was entered in into the one now selected.
		var enteredCalendar = calendarSelect.value;
		calendarSelect.addEventListener('change', function () {
			var year = enteredYear();
			var month = +monthSelect.value;
			var day = +daySelect.value;
			if (year === null || isNaN(month) || isNaN(day)) {
				populateMonths();
				populateDays();
			} else {
				setFields(jdnOf(enteredCalendar, year, month, day));
			}
			enteredCalendar = calendarSelect.value;
			refresh();
		});
		yearInput.addEventListener('input', function () {
			populateMonths();
			populateDays();
			refresh();
		});
		monthSelect.addEventListener('change', function () {
			populateDays();
			refresh();
		});
		daySelect.addEventListener('change', function () {
			refresh();
		});

		setFields(todayJdn());
		refresh();
	}

	window.calendarReady = start;
})();

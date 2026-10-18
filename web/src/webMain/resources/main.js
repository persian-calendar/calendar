(function () {
	'use strict';

	// The ids are the ones ICU gives these calendars, so a link reads the way the rest of the
	// world names them; the library knows the Gregorian one as "civil".
	var CALENDARS = [
		{ id: 'gregorian', label: 'میلادی' },
		{ id: 'persian', label: 'شمسی' },
		{ id: 'islamic', label: 'قمری' },
	];

	function libraryId(calendar) {
		return calendar === 'gregorian' ? 'civil' : calendar;
	}

	var WEEKDAYS = [
		'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنج‌شنبه', 'جمعه', 'شنبه', 'یکشنبه',
	];

	var GREGORIAN_MONTHS = [
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
		if (calendar === 'gregorian') return GREGORIAN_MONTHS;
		if (calendar === 'islamic') return ISLAMIC_MONTHS;
		return year !== null && year < BORJI_UNTIL_YEAR ? BORJI_MONTHS : PERSIAN_MONTHS;
	}

	function monthLabel(name, month) {
		return name + ' (' + month.toLocaleString('fa') + ')';
	}

	// The year field accepts different kinds of digits alike. It also has to read back what
	// the page itself puts there, since toLocaleString writes a negative year as a direction
	// mark, a typographic minus and Persian digits.
	function normalizeDigits(input) {
		return String(input)
			.replace(/[\u200e\u200f\u061c]/g, '')
			.replace(/[\u2212\u2013]/g, '-')
			.replace(/[۰-۹٠-٩]/g, function (digit) {
				var code = digit.charCodeAt(0);
				// ۰ is U+06F0 and ٠ is U+0660; both count up to ۹ / ٩ the same way
				var base = (code >= 0x06F0 && code <= 0x06F9) ? 0x06F0 : 0x0660;
				return String(code - base);
			});
	}

	function pad2(value) {
		return value < 10 ? '0' + value : String(value);
	}

	// A year, a month and a day, as a link carries them: 2025-04-04, or 2-4-4 for a date far
	// enough back that its year is small. The year may be negative.
	function parseDate(text) {
		var match = /^(-?\d+)-(\d{1,2})-(\d{1,2})$/.exec(normalizeDigits(text).trim());
		if (!match) return null;
		return { year: +match[1], month: +match[2], day: +match[3] };
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

		// Set when an opening link named a date the page cannot stand behind.
		var staleLink = false;

		// A date in a calendar comes back as "year|month|day", since an array cannot cross
		// the boundary into a Wasm build.
		function dateIn(calendar, jdn) {
			return dateOf(libraryId(calendar), jdn).split('|').map(Number);
		}

		CALENDARS.forEach(function (calendar) {
			calendarSelect.appendChild(option(calendar.id, calendar.label));
		});
		// One layer prints the date the card is showing, the other prints the one it is
		// leaving, so a change can dissolve from the first into the second.
		function layer(li, name) {
			var box = document.createElement('div');
			box.className = 'card__layer card__layer--' + name;
			if (name === 'previous') box.setAttribute('aria-hidden', 'true');
			var parts = {};
			['day', 'month', 'date'].forEach(function (field) {
				var cell = document.createElement('div');
				cell.className = field;
				box.appendChild(cell);
				parts[field] = cell;
			});
			li.appendChild(box);
			return parts;
		}

		var cards = CALENDARS.map(function (calendar) {
			var li = document.createElement('li');
			var previous = layer(li, 'previous');
			var current = layer(li, 'current');
			document.getElementById('cards').appendChild(li);
			return {
				calendar: calendar,
				root: li,
				previous: previous,
				day: current.day,
				month: current.month,
				date: current.date,
			};
		});
		calendarSelect.value = 'persian';

		function todayJdn() {
			var now = new Date();
			// The library's name for the Gregorian calendar, which every clock here uses.
			return jdnOf('civil', now.getFullYear(), now.getMonth() + 1, now.getDate());
		}

		function enteredYear() {
			var year = +normalizeDigits(yearInput.value);
			return isNaN(year) ? null : year;
		}

		function populateMonths() {
			var names = monthNames(calendarSelect.value, enteredYear());
			// An empty select reads as 0 rather than as nothing, so the check is a range.
			var previous = +monthSelect.value;
			monthSelect.innerHTML = '';
			names.forEach(function (name, month) {
				monthSelect.appendChild(option(month + 1, monthLabel(name, month + 1)));
			});
			monthSelect.value = previous >= 1 && previous <= names.length ? String(previous) : '1';
		}

		function populateDays() {
			var year = enteredYear();
			var month = +monthSelect.value;
			// The library is only asked for years it can hold.
			if (year === null || isNaN(month) || year < -10000 || year > 10000) {
				daySelect.innerHTML = '';
				return;
			}
			var previous = +daySelect.value;
			var maxDay = daysInMonth(libraryId(calendarSelect.value), year, month);
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
			var last = 0;
			var held = year !== null && year >= -10000 && year <= 10000 &&
				month >= 1 && month <= 12;
			try {
				if (held) last = daysInMonth(libraryId(calendarSelect.value), year, month);
			} catch (exception) {
				last = 0;
			}
			var usable = !staleLink && year !== null && year >= -10000 && year <= 10000 &&
				month >= 1 && month <= 12 && day >= 1 && day <= last;

			// The two panels replace each other, so they are shown and hidden as a pair.
			output.hidden = !usable;
			error.hidden = usable;
			if (!usable) return;

			rememberUrl(year, month, day);

			var jdn = jdnOf(libraryId(calendarSelect.value), year, month, day);
			var delta = jdn - todayJdn();
			diff.textContent = delta === 0
				? 'امروز'
				: (delta > 0
					? delta.toLocaleString('fa') + ' روز بعد از امروز'
					: (-delta).toLocaleString('fa') + ' روز قبل از امروز');
			weekday.textContent = WEEKDAYS[jdn % 7];

			cards.forEach(function (x) {
				var parts = dateIn(x.calendar.id, jdn);
				var year = parts[0];
				var month = parts[1];
				var day = parts[2];
				var dayText = day.toLocaleString('fa');
				var monthText = monthLabel(monthNames(x.calendar.id, year)[month - 1], month);
				x.root.dataset.copy = dayText + ' ' + monthText + ' ' +
					year.toLocaleString('fa', { useGrouping: false });
				setDate(x, dayText, monthText, [
					year.toLocaleString('fa', { useGrouping: false }),
					month.toLocaleString('fa'),
					day.toLocaleString('fa'),
				].join('/'));
				x.root.classList.toggle('is-visible', x.calendar.id !== calendarSelect.value);
			});
		}

		// Moves the old date to the layer that fades out and puts the new one on the layer
		// that fades in. The class has to come off and the layout settle in between, or a
		// second change while one is still running would not animate at all.
		function setDate(x, dayText, monthText, dateText) {
			var changed = x.day.textContent !== dayText || x.month.textContent !== monthText ||
				x.date.textContent !== dateText;
			if (changed && x.day.textContent !== '') {
				x.previous.day.textContent = x.day.textContent;
				x.previous.month.textContent = x.month.textContent;
				x.previous.date.textContent = x.date.textContent;
				x.root.classList.remove('is-changing');
				void x.root.offsetWidth;
				x.root.classList.add('is-changing');
			}
			x.day.textContent = dayText;
			x.month.textContent = monthText;
			x.date.textContent = dateText;
		}

		// The class is what starts the dissolve, so it comes off once the new date has
		// arrived and the next change can start a fresh one.
		output.addEventListener('animationend', function (event) {
			var card = event.target.closest ? event.target.closest('li') : null;
			if (card && event.animationName === 'card-dissolve-in') {
				card.classList.remove('is-changing');
			}
		});

		// Tapping a card copies that date. One listener, so the markup stays a string.
		output.addEventListener('click', function (event) {
			var card = event.target.closest ? event.target.closest('li[data-copy]') : null;
			if (!card) return;
			navigator.clipboard.writeText(card.dataset.copy).then(function () {
				card.classList.add('copied');
				setTimeout(function () {
					card.classList.remove('copied');
				}, 800);
			}, function (error) {
				console.error('Failed to copy text: ', error);
			});
		});

		// The address bar is the whole state of the page: a link opened with a calendar and a
		// date starts there, and the bar follows every change so the page can be passed on.
		// Written in the fragment, which the browser keeps to itself: the address never leaves
		// the browser, so a link tells the server nothing about where it came from. Month and
		// day are padded, and it is only written for a date the calendar really has.
		function rememberUrl(year, month, day) {
			try {
				history.replaceState(null, '', location.pathname +
					'#date=' + year + '-' + pad2(month) + '-' + pad2(day) +
					'&calendar=' + calendarSelect.value);
			} catch (error) {
				// A page opened from the file system cannot rewrite its own address.
			}
		}

		// A date from a link that this calendar does not have is not corrected into one it does:
		// the page says so, and the date is never handed to the library. The first change the
		// reader makes replaces the link's date with one of their own.
		function offerDay(day) {
			for (var i = 0; i < daySelect.options.length; i++) {
				if (+daySelect.options[i].value === day) {
					daySelect.value = String(day);
					return;
				}
			}
			// A month too short for it still shows the day the link named, and the page
			// reports the date as one this calendar does not have.
			daySelect.appendChild(option(day, day.toLocaleString('fa')));
			daySelect.value = String(day);
		}

		function readLink() {
			// The fragment is where a link lives, and it is the one part of an address the
			// browser never sends to the server.
			var params = new URLSearchParams(location.hash.slice(1));
			var calendar = params.get('calendar');
			if (calendar) {
				var known = CALENDARS.some(function (entry) { return entry.id === calendar; });
				if (known) calendarSelect.value = calendar;
			}
			// The next change of calendar converts from what the page is showing now.
			enteredCalendar = calendarSelect.value;

			var date = parseDate(params.get('date') || '');
			if (!date) return false;

			yearInput.value = date.year.toLocaleString('fa', { useGrouping: false });
			populateMonths();
			if (date.month >= 1 && date.month <= 12) {
				monthSelect.value = String(date.month);
			} else {
				// Nothing in the page can name a thirteenth month, so the year is kept and
				// the date is reported as not one this calendar has.
				staleLink = true;
			}
			populateDays();
			if (date.day >= 1 && date.day <= 31) {
				offerDay(date.day);
			} else {
				staleLink = true;
			}
			return true;
		}

		// Fills the three inputs with a date in the selected calendar.
		function setFields(jdn) {
			var parts = dateIn(calendarSelect.value, jdn);
			yearInput.value = parts[0].toLocaleString('fa', { useGrouping: false });
			populateMonths();
			monthSelect.value = String(parts[1]);
			populateDays();
			daySelect.value = String(parts[2]);
		}

		// Changing the input calendar keeps the same instant, so the date is converted
		// from the calendar it was entered in into the one now selected.
		var enteredCalendar = calendarSelect.value;
		calendarSelect.addEventListener('change', function () {
			var year = enteredYear();
			var month = +monthSelect.value;
			var day = +daySelect.value;
			// Converting needs a date the old calendar could have had in the first place.
			if (year !== null && year >= -10000 && year <= 10000 &&
				month >= 1 && month <= 12 && day >= 1) {
				setFields(jdnOf(libraryId(enteredCalendar), year, month, day));
			} else {
				populateMonths();
				populateDays();
			}
			enteredCalendar = calendarSelect.value;
			refresh();
		});
		document.querySelector('.options').addEventListener('input', function () {
			staleLink = false;
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

		// The calendar is set before the fields are filled, so a link naming a calendar lands
		// on that calendar even when it carries no date of its own.
		function openLink() {
			if (!readLink()) setFields(todayJdn());
			refresh();
		}

		// A fragment link to the page that is already open does not load it again, so a new
		// one has to be noticed here.
		window.addEventListener('hashchange', openLink);

		openLink();
	}

	window.calendarReady = start;
})();

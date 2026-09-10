const params = new URLSearchParams(location.search);
const level = Number(params.get('level') || 0);
const flag = (name) => params.get(name) === 'true';
const value = (key, fallback = '—') => params.get(key) || fallback;

const mutateLocators = flag('locators');
const mutateText = flag('text');
const mutateOrder = flag('order');
const mutateHierarchy = flag('hierarchy');
const mutateComponents = flag('components');
const mutateWebDom = flag('webDom');

document.querySelector('#fullName').textContent = `${value('firstName', 'Demo')} ${value('lastName', 'User')}`;
document.querySelector('#email').textContent = value('email');
document.querySelector('#birthDate').textContent = value('birthDate');
document.querySelector('#plan').textContent = value('plan', 'Growth');
document.querySelector('#mutationLevel').textContent = `Level ${level}`;

const title = document.querySelector('#summaryTitle');
const person = document.querySelector('#personSummary');
const planCard = document.querySelector('#planSummary');
const finish = document.querySelector('#finishButton');
const terms = document.querySelector('#terms');
const demo = document.querySelector('#demoCard');

if (mutateWebDom && mutateLocators) {
  title.id = 'accountReviewTitle';
  title.dataset.qa = 'review-heading';
  title.removeAttribute('data-testid');
  person.id = 'customerReviewPanel';
  person.dataset.qa = 'customer-review';
  person.removeAttribute('data-testid');
  finish.id = 'activateAccount';
  finish.dataset.qa = 'finish-action';
  finish.removeAttribute('data-testid');
  terms.id = 'confirmationCheck';
  terms.dataset.qa = 'confirmation-check';
  terms.removeAttribute('data-testid');
}

if (mutateWebDom && mutateText) {
  title.textContent = 'Bereit zur Aktivierung';
  finish.textContent = 'Konto jetzt aktivieren';
  demo.querySelector('h2').textContent = 'Finale Prüfung';
  terms.closest('label').lastChild.textContent = ' Daten verbindlich bestätigen';
  document.body.classList.add('content-mutated');
}

if (mutateWebDom && mutateOrder) {
  demo.parentNode.insertBefore(planCard, person);
}

if (mutateWebDom && mutateHierarchy) {
  document.body.classList.add('structure-mutated');
  const wrapper = document.createElement('div');
  wrapper.className = 'action-panel';
  const label = terms.closest('label');
  finish.parentNode.insertBefore(wrapper, label);
  wrapper.appendChild(label);
  wrapper.appendChild(finish);
  finish.setAttribute('aria-label', 'Onboarding bestätigen');
  planCard.className = 'plan-panel';
}

if (mutateWebDom && mutateComponents) {
  const replacement = document.createElement('a');
  replacement.href = '#complete';
  replacement.id = finish.id;
  replacement.className = 'finish-link';
  replacement.textContent = finish.textContent;
  [...finish.attributes].forEach(attr => {
    if (!['id', 'class'].includes(attr.name)) replacement.setAttribute(attr.name, attr.value);
  });
  finish.replaceWith(replacement);
}

const activeFinish = document.querySelector('#finishButton, #activateAccount, .finish-link');
activeFinish.addEventListener('click', (event) => {
  event.preventDefault();
  document.querySelector('#status').textContent = terms.checked
    ? `Onboarding mit Mutation Level ${level} erfolgreich abgeschlossen.`
    : 'Bitte bestätige zuerst die Angaben.';
});
